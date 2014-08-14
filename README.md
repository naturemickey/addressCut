addressCut
==========
中文 地址切分

使用示例：
public class Test {

	public static void main(String[] args) {
		Address address = AddressScanner.scan("广东深圳福田沙头");
		System.out.println(address.getProvinceAddress());
		System.out.println(address.getCityAddress());
		System.out.println(address.getAreaAddress());
		System.out.println(address.getTownAddress());
		System.out.println(address.getDetailAddress());
	}

}
