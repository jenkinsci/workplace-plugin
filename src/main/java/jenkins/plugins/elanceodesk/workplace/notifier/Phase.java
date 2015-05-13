/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jenkins.plugins.elanceodesk.workplace.notifier;

import hudson.EnvVars;
import hudson.Util;
import hudson.model.Cause;
import hudson.model.ParameterValue;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jenkins.model.Jenkins;
import jenkins.plugins.elanceodesk.workplace.notifier.model.BuildState;
import jenkins.plugins.elanceodesk.workplace.notifier.model.Changeset;
import jenkins.plugins.elanceodesk.workplace.notifier.model.JobState;
import jenkins.plugins.elanceodesk.workplace.notifier.model.ScmState;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings({ "unchecked", "rawtypes" })
public enum Phase {
	STARTED, COMPLETED;

	private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.create();

	private ExecutorService executorService = Executors.newCachedThreadPool();

	public void handle(AbstractBuild build, TaskListener listener) {

		WebhookJobProperty property = (WebhookJobProperty) build.getParent().getProperty(WebhookJobProperty.class);
		if (property == null) {
			return;
		}

		JobState jobState = null;
		try {
			jobState = buildJobState(build.getParent(), build, listener);
		} catch (Throwable e) {
			e.printStackTrace(listener.error(String.format("Unable to build the json object")));
			listener.getLogger().println(
					String.format("Unable to build the json object - %s: %s", e.getClass().getName(),
							e.getMessage()));
		}
		if(jobState != null) {
			for (Webhook target : property.getWebhooks()) {
				if (isRun(target, build)) {
					listener.getLogger().println(String.format("Notifying webhook '%s'", target));
					try {
						
						HttpWorker worker = new HttpWorker(target.getUrl(), gson.toJson(jobState), target.getTimeout(), 3,
								listener.getLogger());
						executorService.submit(worker);
					} catch (Throwable error) {
						error.printStackTrace(listener.error(String.format("Failed to notify webhook '%s'", target)));
						listener.getLogger().println(
								String.format("Failed to notify webhook '%s' - %s: %s", target, error.getClass().getName(),
										error.getMessage()));
					}
				}
			}
		}
	}

	/**
	 * Determines if the webhook specified should be notified at the current job
	 * phase.
	 */
	private boolean isRun(Webhook webhook, AbstractBuild build) {
		if (this.equals(STARTED) && webhook.isStartNotification()) {
			return true;
		} else if (this.equals(COMPLETED)) {
			Result result = build.getResult();
			Run previousBuild = build.getPreviousBuild();
			Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
			return ((result == Result.ABORTED && webhook.isNotifyAborted())
					|| (result == Result.FAILURE && webhook.isNotifyFailure())
					|| (result == Result.NOT_BUILT && webhook.isNotifyNotBuilt())
					|| (result == Result.SUCCESS && previousResult == Result.FAILURE && webhook.isNotifyBackToNormal())
					|| (result == Result.SUCCESS && webhook.isNotifySuccess()) || (result == Result.UNSTABLE && webhook
					.isNotifyUnstable()));

		} else {
			return false;
		}
	}

	/**
	 * Creates an object that is sent as the post data.
	 * 
	 * @param job
	 * @param run
	 * @param listener
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private JobState buildJobState(Job job, AbstractBuild run, TaskListener listener) throws IOException,
			InterruptedException {

		Jenkins jenkins = Jenkins.getInstance();
		String rootUrl = jenkins.getRootUrl();
		JobState jobState = new JobState();
		BuildState buildState = new BuildState();
		ScmState scmState = new ScmState();
		ParametersAction paramsAction = run.getAction(ParametersAction.class);
		EnvVars environment = run.getEnvironment(listener);
		Result result = run.getResult();
		String status = null;
		long currentBuildCompletionTime = run.getStartTimeInMillis() + run.getDuration();
		List<Cause> causes = run.getCauses();
		if (causes != null) {
			List<String> causesStrList = new ArrayList<String>();
			for (Cause cause : causes) {
				causesStrList.add(cause.getShortDescription());
			}
			buildState.setCauses(causesStrList);
		}

		buildState.setCompletionTime(currentBuildCompletionTime);

		if (this.equals(COMPLETED)) {
			if (result != null) {
				status = result.toString();
				Run previousBuild = run.getPreviousBuild();
				Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
				
				AbstractBuild failingSinceRun = null;
				if(run.getPreviousNotFailedBuild() != null) {
					failingSinceRun = (AbstractBuild) (run.getPreviousNotFailedBuild().getNextBuild());
				} else {
					failingSinceRun = run.getProject().getFirstBuild();
				}
				if (result == Result.SUCCESS && previousResult == Result.FAILURE) {
					status = "BACK_TO_NORMAL";
					buildState.setBackToNormalTime(Util.getTimeSpanString(currentBuildCompletionTime
							- failingSinceRun.getStartTimeInMillis()));
				}
				if (result == Result.FAILURE && failingSinceRun != null) {

					BuildState failingSinceBuildState = new BuildState();
					populateChangeSet(failingSinceRun, failingSinceBuildState, listener);
					failingSinceBuildState.setNumber(failingSinceRun.number);
					failingSinceBuildState.setFullUrl(rootUrl + failingSinceRun.getUrl());
					long failingSinceBuildCompletionTime = failingSinceRun.getStartTimeInMillis()
							+ failingSinceRun.getDuration();
					failingSinceBuildState.setCompletionTime(failingSinceBuildCompletionTime);
					failingSinceBuildState.setFailingSinceTime(Util.getTimeSpanString(currentBuildCompletionTime
							- failingSinceRun.getStartTimeInMillis()));
					buildState.setFailingSinceBuild(failingSinceBuildState);
				}
				buildState.setStatus(status);
			}
			buildState.setDurationString(run.getDurationString());
		}

		jobState.setName(job.getName());
		jobState.setUrl(job.getUrl());
		jobState.setBuild(buildState);

		buildState.setNumber(run.number);
		buildState.setUrl(run.getUrl());
		buildState.setPhase(this);
		buildState.setScm(scmState);

		if (rootUrl != null) {
			buildState.setFullUrl(rootUrl + run.getUrl());
		}

		// buildState.updateArtifacts(job, run);

		if (paramsAction != null) {
			EnvVars env = new EnvVars();
			for (ParameterValue value : paramsAction.getParameters()) {
				if (!value.isSensitive()) {
					value.buildEnvironment(run, env);
				}
			}
			buildState.setParameters(env);
		}

		if (environment.get("GIT_URL") != null) {
			scmState.setUrl(environment.get("GIT_URL"));
		}

		if (environment.get("GIT_BRANCH") != null) {
			scmState.setBranch(environment.get("GIT_BRANCH"));
		}

		if (environment.get("GIT_COMMIT") != null) {
			scmState.setCommit(environment.get("GIT_COMMIT"));
		}

		if (this.equals(STARTED)) {
			populateChangeSet(run, buildState, listener);
		}

		return jobState;
	}

	private void populateChangeSet(AbstractBuild run, BuildState buildState, TaskListener listener) {
		ChangeLogSet changeLogSet = run.getChangeSet();
		List<Changeset> changesets = new ArrayList<Changeset>();
		for (Object o : changeLogSet.getItems()) {

			Entry entry = (Entry) o;
			listener.getLogger().println("Entry " + o);
			Changeset changeset = new Changeset(entry.getAuthor().getDisplayName(), entry.getAuthor().getId(),
					entry.getAffectedFiles());
			changesets.add(changeset);
		}
		buildState.setChangeSet(changesets);
	}
}