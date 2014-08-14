package net.yeah.zhouyou.mickey.address;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NFA extends FA<Set<NFAState>> {

	private static final long serialVersionUID = -1226425668908896411L;

	private NFA() {
	}

	private NFAState firstState = new NFAState();
	private NFAState lastState = new NFAState();

	public NFAState getFirstState() {
		return firstState;
	}

	public NFAState getLastState() {
		return lastState;
	}

	public static NFA constractNFA(String name) {
		try {
			NFA res = new NFA();
			NFAState pn = null;
			for (char bs : name.toCharArray()) {
				NFAState cn = new NFAState();
				if (pn == null) {
					res.firstState.getPath().put(bs, createSet(cn));
				} else {
					pn.getPath().put(bs, createSet(cn));
				}
				pn = cn;
			}
			pn.setName(name);
			res.lastState = pn;
			return res;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Set<NFAState> createSet(NFAState... states) {
		Set<NFAState> set = new HashSet<NFAState>();
		for (NFAState state : states)
			set.add(state);
		return set;
	}

//	private static NFA and(NFA... nfas) {
//		NFA preNfa = null;
//		for (NFA nfa : nfas) {
//			if (preNfa == null)
//				preNfa = nfa;
//			else
//				preNfa.lastState.getPath().put(null, createSet(nfa.firstState));
//		}
//		return nfas[0];
//	}

	public static NFA or(NFA... nfas) {
		NFA res = new NFA();
		Set<NFAState> s = createSet(res.lastState);
		for (NFA nfa : nfas) {
			Set<NFAState> fs = res.firstState.getPath().get(null);
			if (fs == null) {
				fs = new HashSet<NFAState>();
				res.firstState.getPath().put(null, fs);
			}
			fs.add(nfa.firstState);
			nfa.lastState.getPath().put(null, s);
			nfa.lastState.getPath().put(null, s);
		}
		return res;
	}

	@SuppressWarnings("unused")
	private static NFA kleenClosure(NFA nfa) {
		// Kleen闭包这里用不上，所以暂不实现。
		return null;
	}

	@Override
	public List<String> scan(String s) {
		// 不运行NFA，所以不实现。
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Set<NFAState> nss = new HashSet<NFAState>();
		Deque<NFAState> stack = new ArrayDeque<NFAState>();
		stack.push(this.firstState);

		while (!stack.isEmpty()) {
			NFAState ns = stack.pollFirst();
			nss.add(ns);
			sb.append(ns.toString());
			for (Set<NFAState> nss2 : ns.getPath().values()) {
				for (NFAState ns2 : nss2) {
					if (nss.contains(ns2) == false)
						stack.push(ns2);
				}
			}
		}

		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(constractNFA("福田"));
	}
}
