package com.tool.model;

public class BwfVersionInfo {
	private String artifact;
	private String versionInRoot;
	private String version;
	private String isUnmatch;

	public BwfVersionInfo() {
	}

	public BwfVersionInfo(String artifact, String versionInRoot, String version, String isUnmatch) {
		this.artifact = artifact;
		this.versionInRoot = versionInRoot;
		this.version = version;
		this.isUnmatch = isUnmatch;
	}

	public String getArtifact() {
		return artifact;
	}

	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	public String getVersionInRoot() {
		return versionInRoot;
	}

	public void setVersionInRoot(String versionInRoot) {
		this.versionInRoot = versionInRoot;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIsUnmatch() {
		return isUnmatch;
	}

	public void setIsUnmatch(String isUnmatch) {
		this.isUnmatch = isUnmatch;
	}

	@Override
	public String toString() {
		return this.artifact + "," + this.version + "," + this.versionInRoot + "," + this.isUnmatch;
	}
}
