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
package jenkins.plugins.elanceodesk.workplace.notifier.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jenkins.plugins.elanceodesk.workplace.notifier.Phase;

@SuppressWarnings("rawtypes")
public class BuildState {

    private String fullUrl;

    private int number;

    private Phase phase;

    private String status;

    private String url;

    private String displayName;

    private ScmState scm;

    private Map<String, String> parameters;
    
    private List<Changeset> changeSet;
    
    private List<String> causes;

    private String durationString;
    
    private long completionTime;
    
    private BuildState failingSinceBuild;
    
    private String failingSinceTime;
    
    private String backToNormalTime;
    
//    /**
//     *  Map of artifacts: file name => Map of artifact locations ( location name => artifact URL )
//     *  ---
//     *  artifacts:
//     *   notification.hpi:
//     *     s3: https://s3-eu-west-1.amazonaws.com/evgenyg-bakery-artifacts/jobs/notification-plugin/78/notification.hpi
//     *     archive: http://localhost:8080/job/notification-plugin/78/artifact/target/notification.hpi
//     *   notification.jar:
//     *     archive: http://localhost:8080/job/notification-plugin/78/artifact/target/notification.jar
//     */
//    private final Map<String, Map<String, String>> artifacts = new HashMap<String, Map<String, String>>();

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> params) {
        this.parameters = new HashMap<String, String>( params );
    }

//    public Map<String, Map<String, String>> getArtifacts () {
//        return artifacts;
//    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ScmState getScm ()
    {
        return scm;
    }

    public void setScm ( ScmState scmState )
    {
        this.scm = scmState;
    }


	// /**
	// * Updates artifacts Map with S3 links, if corresponding publisher is
	// available.
	// */
	// public void updateArtifacts ( Job job, Run run )
	// {
	// updateArchivedArtifacts( run );
	// }


    
//	private void updateArchivedArtifacts ( Run run )
//    {
//        @SuppressWarnings( "unchecked" )
//        List<Run.Artifact> buildArtifacts = run.getArtifacts();
//
//        if ( buildArtifacts == null ) { return; }
//
//        for ( Run.Artifact a : buildArtifacts ) {
//            String artifactUrl = Jenkins.getInstance().getRootUrl() + run.getUrl() + "artifact/" + a.getHref();
//            updateArtifact( a.getFileName(), "archive", artifactUrl );
//        }
//    }
//
//    /**
//     * Updates an artifact URL.
//     *
//     * @param fileName     artifact file name
//     * @param locationName artifact location name, like "s3" or "archive"
//     * @param locationUrl  artifact URL at the location specified
//     */
//    private void updateArtifact( String fileName, String locationName, String locationUrl )
//    {
//        verifyNotEmpty( fileName, locationName, locationUrl );
//
//        if ( ! artifacts.containsKey( fileName )) {
//            artifacts.put( fileName, new HashMap<String, String>());
//        }
//
//        if ( artifacts.get( fileName ).containsKey( locationName )) {
//            throw new RuntimeException( String.format(
//                "Adding artifacts mapping '%s/%s/%s' - artifacts Map already contains mapping of location '%s': %s",
//                fileName, locationName, locationUrl, locationName, artifacts ));
//        }
//
//        artifacts.get( fileName ).put( locationName, locationUrl );
//    }
    
    public List<Changeset> getChangeSet() {
		return changeSet;
	}

	public void setChangeSet(List<Changeset> changeSet) {
		this.changeSet = changeSet;
	}

	public List<String> getCauses() {
		return causes;
	}

	public void setCauses(List<String> causes) {
		this.causes = causes;
	}

	public String getDurationString() {
		return durationString;
	}

	public void setDurationString(String durationString) {
		this.durationString = durationString;
	}

	public BuildState getFailingSinceBuild() {
		return failingSinceBuild;
	}

	public void setFailingSinceBuild(BuildState failingSinceBuild) {
		this.failingSinceBuild = failingSinceBuild;
	}

	public long getCompletionTime() {
		return completionTime;
	}

	public void setCompletionTime(long completionTime) {
		this.completionTime = completionTime;
	}

	public String getFailingSinceTime() {
		return failingSinceTime;
	}

	public void setFailingSinceTime(String failingSinceTime) {
		this.failingSinceTime = failingSinceTime;
	}

	public String getBackToNormalTime() {
		return backToNormalTime;
	}

	public void setBackToNormalTime(String backToNormalTime) {
		this.backToNormalTime = backToNormalTime;
	}
}
