package net.yeah.zhouyou.mickey.address;

import java.io.Serializable;
import java.util.List;

public abstract class FA<PathDestType> implements Serializable {

	private static final long serialVersionUID = 8061940172429267802L;

	public abstract List<String> scan(String s);
}
