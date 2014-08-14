package net.yeah.zhouyou.mickey.address;

import static net.yeah.zhouyou.mickey.address.DFAInstance.dfa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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
public class AddressScanner {

	private static final Set<String> dCity;

	static {
		dCity = new HashSet<String>();
		dCity.add("北京");
		dCity.add("上海");
		dCity.add("天津");
		dCity.add("重庆");

	}

	private static Pattern p = Pattern.compile("[\\s　]");

	public static Address scan(String txt) {
		// 中文地址中的空白是没有意义的
		txt = p.matcher(txt).replaceAll("");
		// txt = txt.replaceAll("[\\s　]", "");
		List<String> addrList = dfa.scan(txt);

		Address res = new Address(txt);
		if (addrList.size() == 0)
			return res;

		CityToken top = null;
		CityToken bottom = null;
		List<CityToken> ctList = new ArrayList<CityToken>();

		while (!addrList.isEmpty()) {
			String name = getNextAddr(addrList);
			CityToken firstct = findTopCT(name);

			// 中国人写地址一般是“省”、“市”、“区”，对于BSP来说，商家也很少会省略“省”和“市”，如果直接写“区”以下的地址，则全国的地址重名的过多了。
			if (firstct.getLevel() > 3) {
				continue;
			}
			res.setAddr(firstct, name, firstct.getLevel());
			ctList.add(firstct);
			top = firstct;
			bottom = firstct;
			break;
		}

		while (!addrList.isEmpty()) {
			String name = getNextAddr(addrList);

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
						if (ct.getLevel() < 3 || name.length() >= 3
								|| DataCache.getCodeMap().get(ct.getCode()).getName().endsWith(name)) {
							bottom = ct;
							res.setAddr(ct, name, ct.getLevel());
							ctList.add(ct);
							break;
						}
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

	private static String getNextAddr(List<String> addrList) {
		String name = addrList.remove(0);
		while (addrList.remove(name))
			;
		return name;
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
}
