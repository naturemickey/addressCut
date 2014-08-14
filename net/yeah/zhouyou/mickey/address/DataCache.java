package net.yeah.zhouyou.mickey.address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		long initStart = System.currentTimeMillis();

		Map<String, List<CityToken>> nm = new HashMap<String, List<CityToken>>();
		Map<String, CityToken> cm = new HashMap<String, CityToken>();

		InputStream fis = null;
		BufferedReader br = null;
		try {
			// fis = new FileInputStream("bsp_city.config");
			fis = DataCache.class.getClassLoader().getResourceAsStream("citybasedata.config");
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] ss = line.split(",");
				if (line.length() <= 2)
					continue;
				String code = ss[0];
				String parentCode = ss[1];
				String level = ss[2];

				for (int i = 3; i < ss.length; ++i) {
					String name = ss[i];

					CityToken ct = new CityToken(code, parentCode, Integer.valueOf(level), name);
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

			for (List<CityToken> ctl : nm.values()) {
				Collections.sort(ctl, new Comparator<CityToken>() {
					@Override
					public int compare(CityToken o1, CityToken o2) {
						return o1.getLevel() - o2.getLevel();
					}
				});

				for (CityToken ct : ctl) {
					if (ct.getParentCode() != null) {
						ct.parent = cm.get(ct.getParentCode());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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

		nameMap = Collections.unmodifiableMap(nm);
		codeMap = Collections.unmodifiableMap(cm);
		System.out.println("DataCache init cost:" + (System.currentTimeMillis() - initStart));
	}
}
