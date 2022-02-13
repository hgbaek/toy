import data.xml.SgXmlReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


public class ListFileTransition {
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public static void main(String[] args) {
//        ExtractProduct ep = new ExtractProduct();
//        ep.ExtractProductListsFromListFile();

        ArrayList<String> products = getProductListFromFile();

        ArrayList<String> expendables = new ArrayList<>(
                Arrays.asList("8 strip", "96 plate", "96 film", "96 cap",
                        "BIOplastics 8 strip", "BIOplastics 96 film", "Bio-Rad tube", "BIOplastics tube",
                        "extraction-free")
        );

        ArrayList<String> postfixList = new ArrayList<String>();

        for(String product : products)
        {
            try (InputStream stream = ClassLoader.getSystemClassLoader().getResource("list").openStream()) {
                SgXmlReader reader = new SgXmlReader(stream);

                for (SgXmlReader testkitList : reader.searchChildNodes("TestKitList")) {
                    for (SgXmlReader cfgProduct : testkitList.searchChildNodes("Name")) {
                        boolean useExpendable = false;
                        for(String expendable : expendables) {
                            if (cfgProduct.getTextContent().contains("(" + expendable))
                            {
                                useExpendable = true;
                                String[] st = cfgProduct.getTextContent().split(expendable + "\\)");
                                if(st.length > 1)
                                {
//                                    System.out.println(st[1]);
                                    if(!postfixList.contains(st[1]))
                                        postfixList.add(st[1]);
                                }
                            }
                        }
                        if (!useExpendable) {
                            if(cfgProduct.getTextContent().contains(product))
                            {
                                String[] st = cfgProduct.getTextContent().split(product);
                                if (st.length == 2) {
                                    if (!postfixList.contains(st[0])) {
                                        postfixList.add(st[0]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        for (String postfix : postfixList) {
            System.out.println(postfix);
        }

    }

    private static ArrayList<String> getProductListFromFile() {
        try {
            ArrayList<String> products = new ArrayList<>();
            File file = new File(ClassLoader.getSystemClassLoader().getResource("ProductLists").toURI());

            if(file.exists())
            {
                BufferedReader inFile = new BufferedReader(new FileReader(file));
                String line = null;

                while ((line = inFile.readLine()) != null) {
                    products.add(line);
                }
            }

            return products;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
