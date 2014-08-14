package net.yeah.zhouyou.mickey.address;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FAState<PathDestType> implements Serializable {

	private static final long serialVersionUID = -1766733545856064699L;

	private Map<Character, PathDestType> path = new HashMap<Character, PathDestType>();
	private String name;

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(super.toString()).append("]\n");
		for (Entry<Character, PathDestType> e : this.getPath().entrySet()) {
			sb.append('\t').append(':').append(e.getKey() == null ? "_e" : e.getKey()).append("->")
					.append(e.getValue()).append('\n');
		}
		return sb.toString();
	}
}
