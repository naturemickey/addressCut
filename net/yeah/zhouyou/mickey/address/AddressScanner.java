package net.yeah.zhouyou.mickey.address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddressScanner {

	private static final DFA dfa;

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

	public static Address scan(String txt) {
		// 中文地址中的空白是没有意义的
		txt = txt.replaceAll("[\\s　]", "");
		List<String> addrList = dfa.scan(txt.replaceAll("[\\s　]", ""));

		Address res = new Address(txt);
		Tuple2<Tuple2<String, CityToken>, Map<Integer, Map<String, List<CityToken>>>> tuple2 = findTop(addrList);
		CityToken top = tuple2._1._2;
		if (top != null) {
			res.setAddr(top, tuple2._1._1, top.getLevel());
			findNextLevel(res, top.getLevel() + 1, tuple2._2, addrList);
			findParentLevel(res, top);
		}
		return res;
	}

	private static void findParentLevel(Address res, CityToken top) {
		while (top.getLevel() > 1) {
			if (top.getParent() == null)
				break;
			if (res.getAddr(top.getLevel() - 1) != null)
				break;
			top = top.getParent();
			res.setAddr(top, null, top.getLevel());
		}
	}

	private static void findNextLevel(Address res, int level, Map<Integer, Map<String, List<CityToken>>> levelMap,
			List<String> addrList) {
		if (level > 4)
			return;
		// 逻辑上 pct 不可能是空的
		CityToken pct = getPct(res, level);

		// TODO levelMAP没有排序
		Map<String, List<CityToken>> ctListMap = levelMap.get(level);
		if (ctListMap != null && ctListMap.size() > 0) {
			List<Tuple2<String, CityToken>> resList = new ArrayList<Tuple2<String, CityToken>>();
			for (Map.Entry<String, List<CityToken>> e : ctListMap.entrySet()) {
				String name = e.getKey();
				List<CityToken> ctList = e.getValue();
				for (CityToken ct : ctList) {
					if (hasRelationship(pct, ct)) {
						resList.add(Tuple2.apply(name, ct));
					}
				}
			}

			Tuple2<String, CityToken> ctr = null;
			if (resList.size() == 1) {
				ctr = resList.get(0);
			} else if (resList.size() > 1) {
				Tuple2<String, CityToken> lct = null;
				int lctIdx = -1;
				for (Tuple2<String, CityToken> ct : resList) {
					int idx = addrList.indexOf(ct._1);
					if (lct == null || (idx >= 0 && idx < lctIdx)) {
						lct = ct;
						lctIdx = idx;
					}
				}
//				ctr = resList.get(0);
				ctr = lct;
			}
			if (ctr != null) {
				res.setAddr(ctr._2, ctr._1, level);
				removeFromCurCache(addrList, ctr._2, levelMap, ctr._1);
				findParentLevel(res, ctr._2);
			}
		}
		findNextLevel(res, level + 1, levelMap, addrList);
	}

	private static boolean hasRelationship(CityToken pct, CityToken ct) {
		if (ct.getParentCode() == null || ct.getLevel() <= pct.getLevel())
			return false;
		boolean res = ct.getParentCode().equals(pct.getCode());
		if (!res) {
			CityToken parentCt = ct.getParent();
			if (parentCt != null) {
				// 如果与ct的parent有关系，则与ct有关系
				return hasRelationship(pct, parentCt);
			}
		}
		return res;
	}

	private static CityToken getPct(Address res, int level) {
		CityToken pct = null;
		do {
			pct = res.getAddr(--level);
		} while (pct == null && level > 0);
		return pct;
	}

	/**
	 * 找到级别最高的那个
	 */
	private static Tuple2<Tuple2<String, CityToken>, Map<Integer, Map<String, List<CityToken>>>> findTop(
			List<String> addrs) {
		Map<String, List<CityToken>> nameMap = DataCache.getNameMap();
		CityToken top = null;
		Map<Integer, Map<String, List<CityToken>>> levelMap = new HashMap<Integer, Map<String, List<CityToken>>>();

		int len = addrs.size();
		String usedName = null;
		for (int idx = 0; idx < len; ++idx) {
			String name = addrs.get(idx);
			for (CityToken ct : nameMap.get(name)) {
				if (top == null || ct.getLevel() < top.getLevel()) {
					top = ct;
					usedName = name;
				}
				Map<String, List<CityToken>> csm = levelMap.get(ct.getLevel());
				if (csm == null) {
					csm = new HashMap<String, List<CityToken>>();
					levelMap.put(ct.getLevel(), csm);
				}
				List<CityToken> cs = csm.get(name);
				if (cs == null) {
					cs = new ArrayList<CityToken>();
					csm.put(name, cs);
				}
				cs.add(ct);
			}
		}
		removeFromCurCache(addrs, top, levelMap, usedName);
		return Tuple2.apply(Tuple2.apply(usedName, top), levelMap);
	}

	private static void removeFromCurCache(List<String> addrs, CityToken ct,
			Map<Integer, Map<String, List<CityToken>>> levelMap, String usedName) {
		while (usedName != null && addrs.remove(usedName))
			;
		for (Map.Entry<Integer, Map<String, List<CityToken>>> e : levelMap.entrySet()) {
			e.getValue().remove(usedName);
		}
	}

}
