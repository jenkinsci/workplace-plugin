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

import hudson.scm.ChangeLogSet.AffectedFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Changes that causes the start of a new build. List of affected files and user
 * that caused those changes.
 *
 */
public class Changeset {

	String authorDisplayName;
	
	String authorId;
	
	Map<String, String> affectedFiles;

	public Changeset(String authorDisplayName, String authorId, Collection<? extends AffectedFile> affectedFiles) {
		this.authorDisplayName = authorDisplayName;
		this.authorId = authorId;
		this.affectedFiles = new HashMap<String, String>();
		for(AffectedFile temp : affectedFiles) {
			this.affectedFiles.put(temp.getPath(), temp.getEditType().getName());
		}
	}
	
	public String getAuthorDisplayName() {
		return authorDisplayName;
	}
	
	public String getAuthorId() {
		return authorId;
	}

	public Map<String, String> getAffectedFiles() {
		return this.affectedFiles;
	}

}