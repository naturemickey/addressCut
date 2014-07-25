package net.yeah.zhouyou.mickey.address;

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

public abstract class FA<PathDestType> implements Serializable {

	private static final long serialVersionUID = 8061940172429267802L;

	public abstract List<String> scan(String s);

	protected String printState(FAState<PathDestType> ns) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append('[').append(ns).append("]\n");
			for (Entry<Character, PathDestType> e : ns.getPath().entrySet()) {
				sb.append('\t').append(':')
						.append(e.getKey() == null ? "_e" : e.getKey())
						.append("->").append(e.getValue()).append('\n');
			}
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
