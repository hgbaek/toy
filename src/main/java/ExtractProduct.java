import data.xml.SgXmlReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ExtractProduct {
    public void ExtractProductListsFromListFile() {
        try (InputStream stream = ClassLoader.getSystemClassLoader().getResource("list").openStream()) {
            ArrayList<String> lists = getTestKitListsFromListFile(stream);
            getProductsFromLists(lists);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getProductsFromLists(ArrayList<String> lists) {
        ArrayList<String> products = new ArrayList<String>();
        ArrayList<String> expendables = new ArrayList<>(
                Arrays.asList("8 strip", "96 plate", "96 film", "96 cap",
                        "BIOplastics 8 strip", "BIOplastics 96 film", "Bio-Rad tube", "BIOplastics tube",
                        "extraction-free")
        );

        for (String product : lists) {
            boolean useExpendable = false;
            for (String expendable : expendables) {
                if(product.contains("(" + expendable)) {
                    useExpendable = true;
                    String[] strAry = product.split("\\("+expendable);
                    if (!products.contains(strAry[0])) {
                        products.add(strAry[0]);
                        break;
                    }
                }
            }
            if(!useExpendable)
            {
                if (!products.contains(product)) {
                    products.add(product);
                }
            }
        }

        for (String p : products)
            System.out.println(p);
    }

    private ArrayList<String> getTestKitListsFromListFile(InputStream stream) throws Exception {
        ArrayList<String> productList = new ArrayList<String>();
        SgXmlReader reader = new SgXmlReader(stream);

        for (SgXmlReader testkitList : reader.searchChildNodes("TestKitList")) {
            for (SgXmlReader product : testkitList.searchChildNodes("Name")) {
                //ArrayList에서 동일 문자열 찾기를 위해 equals를 이용할 시 for문을 사용해야 함.
                //아니면 override 해야함 -> contains로 간단히 사용 가능
                if (!productList.contains(product.getTextContent()))
                    productList.add(product.getTextContent());
            }
        }

        Collections.sort(productList); //오름차순 정렬
//            Collections.sort(productList, Collections.reverseOrder()); //내림차순 정렬
//            for(String s: productList)
//                System.out.println(s);
        return productList;
    }
}