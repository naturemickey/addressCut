package net.yeah.zhouyou.mickey.address;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FAState<PathDestType> implements Serializable {

	private static final long serialVersionUID = -1766733545856064699L;

	private Map<Character, PathDestType> path = new HashMap<Character, PathDestType>();
	private String name;

	// private CityToken ct;
	//
	// public CityToken getCt() {
	// return ct;
	// }
	//
	// public void setCt(CityToken ct) {
	// this.ct = ct;
	// }
	//
	// public int getLevel() {
	// return ct.getLevel();
	// }
	//
	public boolean isAccepted() {
		return name != null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PathDestType tran(Character a) {
		return path.get(a);
	}

	public Map<Character, PathDestType> getPath() {
		return path;
	}
}
