package net.yeah.zhouyou.mickey.address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataCache {

	private static final Map<String, List<CityToken>> nameMap;
	private static final Map<String, CityToken> codeMap;

	public static Map<String, List<CityToken>> getNameMap() {
		return nameMap;
	}

	public static Map<String, CityToken> getCodeMap() {
		return codeMap;
	}

	static {
		final String cacheName = "city_data.cache";
		Tuple2<Map<String, List<CityToken>>, Map<String, CityToken>> tuple2 = SerializeUtil.read(cacheName);
		Map<String, List<CityToken>> nm;
		Map<String, CityToken> cm;
		if (tuple2 != null) {
			nm = tuple2._1;
			cm = tuple2._2;
		} else {
			InputStream fis = null;
			BufferedReader br = null;
			try {
				nm = new HashMap<String, List<CityToken>>();
				cm = new HashMap<String, CityToken>();

				// fis = new FileInputStream("bsp_city.config");
				fis = DataCache.class.getClassLoader().getResourceAsStream("bsp_city.config");
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
				br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					String[] ss = line.split(",");
					if (ss.length == 12) {
						// 多个名字
						List<String> names = new ArrayList<String>(3);
						if (ss[4] != null && ss[4].length() > 0)
							names.add(ss[4]);
						if (ss[5] != null && ss[5].length() > 0)
							names.add(ss[5]);
						if (ss[6] != null && ss[6].length() > 0) {
							names.add(ss[6]);
						}
						Set<String> nameSet = new HashSet<String>();
						if (ss[7] != null && ss[7].length() > 0) {
							for (String name : names) {
								// 基础数据问题，导致“北京”后面会跟着“京”。
								if (!name.endsWith(ss[7])) {
									nameSet.add(name + ss[7]);
								}
							}
						}
						for (String name : names) {
							// 如果一个地名只有一个字，那从我的经验上来看，它的识别度通常过低，所以忽略掉。
							if (name.length() > 1)
								nameSet.add(name);
						}
						for (String name : nameSet) {
							CityToken ct = new CityToken(ss[1], ss[2], Integer.valueOf(ss[3]), name);

							List<CityToken> ctList = nm.get(name);
							if (ctList == null) {
								ctList = new ArrayList<CityToken>();
								nm.put(name, ctList);
							}
							ctList.add(ct);

							CityToken act = cm.get(ct.getCode());
							if (act == null || act.getName().length() < ct.getName().length()) {
								cm.put(ct.getCode(), ct);
							}
						}
					}
				}
				for (Map.Entry<String, CityToken> e : cm.entrySet()) {
					CityToken ct = e.getValue();
					if (ct.getParentCode() != null) {
						ct.parent = cm.get(ct.getParentCode());
					}
				}
				SerializeUtil.write(Tuple2.apply(nm, cm), cacheName);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				if (br != null)
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				if (fis != null)
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		nameMap = Collections.unmodifiableMap(nm);
		codeMap = Collections.unmodifiableMap(cm);
	}

}
