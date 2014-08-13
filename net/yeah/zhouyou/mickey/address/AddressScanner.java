package net.yeah.zhouyou.mickey.address;

import static net.yeah.zhouyou.mickey.address.DFAInstance.dfa;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated 识别准确率上不如AddressScanner2，所以用AddressScanner2替代这个类
 */
public class AddressScanner {

	private static final Set<String> dCity;

	static {
		dCity = new HashSet<String>();
		dCity.add("北京");
		dCity.add("上海");
		dCity.add("天津");
		dCity.add("重庆");
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

			if (res.getCityAddress() == null && res.getProvinceAddress() != null
					&& dCity.contains(res.getProvinceAddress())) {
				// 当只识别到一个地址，并且是直辖市的时候
				List<CityToken> ctl = DataCache.getNameMap().get(res.getProvinceAddress() + "市");
				for (CityToken ct : ctl) {
					if (ct.getParentCode() != null && ct.getParentCode().equals(top.getCode())) {
						res.setAddr(ct, null, ct.getLevel());
						break;
					}
				}
			}
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
				// ctr = resList.get(0);
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
		// 大于两个级别差的关联，相对来说准确率比较低。
		if (ct.getLevel() - pct.getLevel() > 2)
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
