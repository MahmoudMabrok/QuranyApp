package education.mahmoud.quranyapp.utils.testsomefeature;

import education.mahmoud.quranyapp.model.Quran;

public class Test2 {

    static Quran aa;

    public static void main(String[] args) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ا");
        stringBuilder.append("ث");


       /* Gson gson = new Gson();
        Quran quran ;
        try {
            InputStream inputStream = new FileInputStream(new File("data.json"));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream );
            Reader reader = new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8);
            quran = gson.fromJson(reader, Quran.class);
            for(Sura sura : quran.getSurahs()){
                System.out.println(sura.getName());
            }
            } catch (Exception e) {
            e.printStackTrace();
        }
*/


    }


}