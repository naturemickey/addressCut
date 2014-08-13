package net.yeah.zhouyou.mickey.address;

import java.util.Set;

public class DFAInstance {

	public static final DFA dfa;

	static {
		long initStart = System.currentTimeMillis();
		String cacheName = "dfaObj.cache";
		DFA fa = SerializeUtil.read(cacheName);
		if (fa == null) {
			Set<String> nameSet = DataCache.getNameMap().keySet();
			NFA[] nfas = new NFA[nameSet.size()];
			int idx = 0;
			for (String name : nameSet) {
				nfas[idx++] = NFA.constractNFA(name);
			}
			NFA nfa = NFA.or(nfas);
			dfa = DFA.createDFA(nfa);
			SerializeUtil.write(dfa, cacheName);
		} else {
			dfa = fa;
		}
		System.out.println("DFA init cost:" + (System.currentTimeMillis() - initStart));
	}
}
