package net.yeah.zhouyou.mickey.address;

public class Address {
	private CityToken provinceAddress; // 省 level 1
	private String provinceAddressReal;
	private CityToken cityAddress; // 市 level 2
	private String cityAddressReal;
	private CityToken areaAddress; // 区 level 3
	private String areaAddressReal;
	private CityToken townAddress; // 镇/街道办 level 4
	private String townAddressReal;

	private String originalAddress;
	private String detailAddress;

	public Address(String originalAddress) {
		if (originalAddress != null)
			this.originalAddress = originalAddress;
		else
			this.originalAddress = "";
	}

	public String getDetailAddress() {
		if (detailAddress == null) {
			synchronized (originalAddress) {
				if (detailAddress == null) {
					if (townAddressReal != null) {
						detailAddress = subOrigAddr(townAddressReal, townAddress.getName(), true);
					} else if (areaAddressReal != null) {
						detailAddress = subOrigAddr(areaAddressReal, areaAddress.getName(), true);
					} else if (cityAddressReal != null) {
						detailAddress = subOrigAddr(cityAddressReal, cityAddress.getName(), false);
					} else if (provinceAddressReal != null) {
						detailAddress = subOrigAddr(provinceAddressReal, provinceAddress.getName(), false);
					}
				}
			}
		}
		return detailAddress;
	}

	private String subOrigAddr(String addr, String stdAddr, boolean b) {
		String res = null;
		int idx = originalAddress.lastIndexOf(stdAddr);
		if (idx < 0)
			idx = originalAddress.lastIndexOf(addr);
		if (idx >= 0) {
			// 保留当前部分的地址。
			if (b) {
				res = originalAddress.substring(originalAddress.indexOf(addr));
			} else {
				res = originalAddress.substring(originalAddress.indexOf(addr) + addr.length());
			}
		}
		if (res == null || res.length() == 0)
			res = originalAddress;
		return res;
	}

	public String getOriginalAddress() {
		return originalAddress;
	}

	public void setAddr(CityToken addr, String realName, int level) {
		addr = DataCache.getCodeMap().get(addr.getCode());
		switch (level) {
		case 1:
			provinceAddress = addr;
			provinceAddressReal = realName;
			break;
		case 2:
			cityAddress = addr;
			cityAddressReal = realName;
			break;
		case 3:
			areaAddress = addr;
			areaAddressReal = realName;
			break;
		case 4:
			townAddress = addr;
			townAddressReal = realName;
			break;
		default:
		}
	}

	public CityToken getAddr(int level) {
		switch (level) {
		case 1:
			return provinceAddress;
		case 2:
			return cityAddress;
		case 3:
			return areaAddress;
		case 4:
			return townAddress;
		default:
			return null;
		}
	}

	public String getProvinceAddress() {
		if (provinceAddress == null)
			return null;
		return provinceAddress.getName();
	}

	public String getCityAddress() {
		if (cityAddress == null)
			return null;
		return cityAddress.getName();
	}

	public String getAreaAddress() {
		if (areaAddress == null)
			return null;
		return areaAddress.getName();
	}

	public String getTownAddress() {
		if (townAddress == null)
			return null;
		return townAddress.getName();
	}
}
