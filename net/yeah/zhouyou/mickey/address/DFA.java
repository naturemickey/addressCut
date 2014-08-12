package net.yeah.zhouyou.mickey.address;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DFA extends FA<DFAState> implements Serializable {

	private static final long serialVersionUID = -4699660788265616079L;

	private DFAState startState = null;

	private DFA() {
	}

	public static DFA createDFA(NFA nfa) {
		// 龙书三，算法3.20
		List<Dstate> dstates = new ArrayList<Dstate>();
		Set<Dstate> dstateset = new HashSet<Dstate>();
		Dstate dss = new Dstate();
		dss.set = e_closure(nfa.getFirstState());
		dstates.add(dss);
		dstateset.add(dss);

		for (int idx = 0; idx < dstates.size(); ++idx) {
			Dstate ds = dstates.get(idx);
			for (Character a : allInputSign(ds.set)) {
				if (a != null) {
					Dstate ds2 = new Dstate();
					ds2.set = e_closure(move(ds.set, a));

					if (!dstateset.contains(ds2))
						dstates.add(ds2);

					ds.path.put(a, ds2);
				}
			}
		}

		Map<Dstate, DFAState> map = new HashMap<Dstate, DFAState>();
		for (Dstate ds : dstates) {
			DFAState ds2 = new DFAState();
			map.put(ds, ds2);
		}

		for (Dstate ds : dstates) {
			DFAState ds2 = map.get(ds);
			for (Entry<Character, Dstate> e : ds.path.entrySet()) {
				NFAState ns = e.getValue().getAcceptedState();
				DFAState ds3 = map.get(e.getValue());
				if (ns != null) {
					ds3.setName(ns.getName());
				}
				ds2.getPath().put(e.getKey(), ds3);
			}
		}
		DFA dfa = new DFA();
		dfa.startState = map.get(dss);
		return dfa;
	}

	private static class Dstate {
		Set<NFAState> set = new HashSet<NFAState>();
		Map<Character, Dstate> path = new HashMap<Character, Dstate>();

		NFAState getAcceptedState() {
			NFAState res = null;
			for (NFAState ns : set) {
				if (ns.isAccepted()) {
					if (res == null/* || res.getLevel() < ns.getLevel() */)
						res = ns;
				}
			}
			return res;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((set == null) ? 0 : set.hashCode());
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
			Dstate other = (Dstate) obj;
			if (set == null) {
				if (other.set != null)
					return false;
			} else if (!set.equals(other.set))
				return false;
			return true;
		}

	}

	private static Set<NFAState> e_closure(NFAState ns) {
		Set<NFAState> set = ns.getPath().get(null);
		if (set == null)
			return Collections.emptySet();
		Set<NFAState> res = new HashSet<NFAState>();
		for (NFAState ns2 : set) {
			if (res.add(ns2))
				res.addAll(e_closure(ns2));
		}
		return res;
	}

	private static Set<Character> allInputSign(Set<NFAState> set) {
		Set<Character> res = new HashSet<Character>();
		for (NFAState ns : set) {
			for (Character b : ns.getPath().keySet()) {
				if (b != null)
					res.add(b);
			}
		}
		return res;
	}

	private static Set<NFAState> e_closure(Set<NFAState> set) {
		Set<NFAState> res = new HashSet<NFAState>();
		for (NFAState ns : set) {
			if (res.add(ns) == true)
				res.addAll(e_closure(ns));
		}
		return res;
	}

	private static Set<NFAState> move(Set<NFAState> set, Character a) {
		Set<NFAState> res = new HashSet<NFAState>();
		for (NFAState ns : set) {
			Set<NFAState> s = ns.getPath().get(a);
			if (s != null)
				res.addAll(s);
		}
		return res;
	}

	@Override
	public List<String> scan(String s) {
		DFAState currentState = this.startState;
		int currentIdx = 0;

		DFAState currentAccepted = null;
		int currentAcceptedIdx = 0;

		int fromIdx = 0;

		char[] bl = s.toCharArray();
		List<String> res = new ArrayList<String>();
		for (; currentIdx < bl.length; ++currentIdx) {
			char a = bl[currentIdx];
			currentState = currentState.tran(a);
			if (currentState == null) {
				if (currentAccepted != null) {
					res.add(currentAccepted.getName());

					fromIdx = currentAcceptedIdx + 1;
					currentAccepted = null;
					currentIdx = currentAcceptedIdx;
				} else {
					currentIdx = fromIdx;
					fromIdx = fromIdx + 1;
				}
				currentState = this.startState;
			} else if (currentState.isAccepted()) {
				currentAccepted = currentState;
				currentAcceptedIdx = currentIdx;
			}
		}
		if (currentAccepted != null) {
			res.add(currentAccepted.getName());
		}
		return res;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Set<DFAState> nss = new HashSet<DFAState>();
		Deque<DFAState> stack = new ArrayDeque<DFAState>();
		stack.push(this.startState);

		while (!stack.isEmpty()) {
			DFAState ds = stack.pollFirst();
			nss.add(ds);
			sb.append(printState(ds));
			for (DFAState ds2 : ds.getPath().values()) {
				if (nss.contains(ds2) == false)
					stack.push(ds2);
			}
		}

		return sb.toString();
	}

}
