package net.yeah.zhouyou.mickey.address;

import java.io.Serializable;

public class CityToken implements Serializable {

	private static final long serialVersionUID = 3571530469280762515L;
	private String code;
	private String name;
	private String parentCode;
	private int level;
	CityToken parent;

	private boolean hasInitParent = false;

	public CityToken getParent() {
		if (!hasInitParent) {
			synchronized (CityToken.class) {
				if (!hasInitParent) {
					if (parentCode != null)
						parent = DataCache.getCodeMap().get(parentCode);
					hasInitParent = true;
				}
			}
		}
		return parent;
	}

	public CityToken(String code, String parentCode, int level, String name) {
		super();
		this.code = code;
		this.name = name;
		this.parentCode = parentCode;
		this.level = level;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getParentCode() {
		return parentCode;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + level;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parentCode == null) ? 0 : parentCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CityToken other = (CityToken) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (level != other.level)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parentCode == null) {
			if (other.parentCode != null)
				return false;
		} else if (!parentCode.equals(other.parentCode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CityToken [code=" + code + ", name=" + name + ", parentCode=" + parentCode + ", level=" + level + "]";
	}

}
