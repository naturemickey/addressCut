package net.yeah.zhouyou.mickey.address;

import java.io.Serializable;

public class Tuple2<K, V> implements Serializable {

	private static final long serialVersionUID = 896426164562239355L;

	public final K _1;
	public final V _2;

	public Tuple2(K _1, V _2) {
		this._1 = _1;
		this._2 = _2;
	}

	public static <E, T> Tuple2<E, T> apply(E _1, T _2) {
		return new Tuple2<E, T>(_1, _2);
	}

	@Override
	public String toString() {
		return "Tuple2 [_1=" + _1 + ", _2=" + _2 + "]";
	}

}
