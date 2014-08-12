package net.yeah.zhouyou.mickey.address;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <pre>
 * 因AddressScanner对于低级地址中包含高级地址的名称时，会识别出错，出错的原因是：级别高低是第一优先级，文字出现前后是第二优先级。
 * AddressScanner2这个类尝试只按文字出现前后做的优先级，看看效果。
 * 如果效果理想，则用AddressScanner2替换AddressScanner。
 * 
 * 优先级逻辑(目标是这样的逻辑)：
 * 1.先出现的地址优先。
 * 2.当前识别出的地址如果有不同level的，则以高level的优先。
 * 3.如果当前识别出的地址有多个是同一level的，则以可以与下一个地址匹配上下级关系的优先。
 * 4.如果当前识别出的地址有多个是同一level的，且都无法与下一个地址配置上下级关系，则任取一个。
 * </pre>
 */
public class AddressScanner2 {

	private static final DFA dfa;
	private static final Set<String> dCity;

	static {
		dCity = new HashSet<String>();
		dCity.add("北京");
		dCity.add("上海");
		dCity.add("天津");
		dCity.add("重庆");

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
		int addrListlen = addrList.size();
		if (addrListlen == 0)
			return res;

		CityToken top = null;
		CityToken bottom = null;
		List<CityToken> ctList = new ArrayList<CityToken>();

		String name = addrList.get(0);
		CityToken firstct = findTopCT(name);
		res.setAddr(firstct, name, firstct.getLevel());
		ctList.add(firstct);

		top = firstct;
		bottom = firstct;

		for (int i = 1; i < addrListlen; ++i) {
			name = addrList.get(i);
			for (CityToken ct : DataCache.getNameMap().get(name)) {
				if (ct.getLevel() < top.getLevel()) {
					if (hasRelationship(ct, top)) {
						top = ct;
						res.setAddr(ct, name, ct.getLevel());
						ctList.add(ct);
						break;
					}
				} else if (ct.getLevel() > bottom.getLevel()) {
					if (hasRelationship(bottom, ct)) {
						bottom = ct;
						res.setAddr(ct, name, ct.getLevel());
						ctList.add(ct);
						break;
					}
				}
			}
		}

		for (CityToken ct : ctList) {
			findParentLevel(res, ct);
		}

		if (res.getCityAddress() == null && res.getProvinceAddress() != null
				&& dCity.contains(res.getProvinceAddress())) {
			// 当只识别到一个地址，并且是直辖市的时候
			List<CityToken> ctl = DataCache.getNameMap().get(res.getProvinceAddress() + "市");
			for (CityToken ct : ctl) {
				if (ct.getParentCode() != null && ct.getParentCode().equals(res.getAddr(1).getCode())) {
					res.setAddr(ct, null, ct.getLevel());
					break;
				}
			}
		}

		return res;
	}

	private static void findParentLevel(Address res, CityToken ct) {
		while (ct.getLevel() > 1) {
			if (ct.getParent() == null)
				break;
			if (res.getAddr(ct.getLevel() - 1) != null)
				break;
			ct = ct.getParent();
			res.setAddr(ct, null, ct.getLevel());
		}
	}

	private static CityToken findTopCT(String name) {
		CityToken top = null;
		for (CityToken ct : DataCache.getNameMap().get(name)) {
			if (top == null || ct.getLevel() < top.getLevel()) {
				top = ct;
			}
		}
		return top;
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
}
