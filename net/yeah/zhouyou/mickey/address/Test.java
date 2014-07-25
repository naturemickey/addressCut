package net.yeah.zhouyou.mickey.address;

public class Test {

	public static void main(String[] args) {
		Address address = AddressScanner.scan("广东省深圳市福田区新洲十一街");
		System.out.println(address.getProvinceAddress());
		System.out.println(address.getCityAddress());
		System.out.println(address.getAreaAddress());
		System.out.println(address.getTownAddress());
		System.out.println(address.getDetailAddress());
	}

}
