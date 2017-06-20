package org.doremus.itema3converter;

import org.doremus.itema3converter.files.LieuGeo;
import org.geonames.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GeoNames {
    static Map<String, Integer> cache; // cache idItema3 -> idGeoNames
    public static String destFolder;

    public static void setDestFolder(String folder) {
        destFolder = folder;
    }

    public static void setUser(String user) {
        WebService.setUserName(user); // add your username here
    }

    public static String toURI(int id) {
        return "http://sws.geonames.org/" + id + "/";
    }

    public static void downloadRdf(int id) {
        String uri = toURI(id) + "about.rdf";
        String outPath = Paths.get(destFolder, id + ".rdf").toString();
        if(new File(outPath).exists()) return; // it is already there!
        try {
            URL website = new URL(uri);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(outPath);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Toponym query(LieuGeo lg) {
        String label = lg.getId();
        String compl = null;
        String featureCode = null;
        switch (lg.getQualif()) {
            case "DPT":
                featureCode = "AMD2";
                break;
            case "REGION":
                featureCode = "RGN";
                break;
            case "QUARTIER":
                featureCode = "PPL";
                break;
            case "FLEUVE":
            case "RIVIERE":
                featureCode = "STM";
                break;
            case "ILE":
                featureCode = "ISL";
                break;
            case "MONT":
                featureCode = "MT";
                break;
            case "MONTAIGNE":
            case "MASSIF":
                featureCode = "MTS";
                break;
            case "SIEGE":
                featureCode = "BLDG";
                break;
            case "CONTINENT":
                featureCode = "CONT";
                break;
            case "ETAT":
                featureCode = "PCLI";
                break;
            case "SITE NUCLEAIRE":
                break;
            default:
                compl = lg.getQualif();
        }

        return GeoNames.query(label, lg.getLabel(), featureCode, compl);
    }

    public static Toponym query(String id, String label, String featureCode, String complement) {
        Toponym tp = null;

        if (cache.containsKey(id)) {
            int k = cache.get(id);
            if (k != -1) {
                tp = new Toponym();
                tp.setGeoNameId(k);
            }
            return tp;
        }

        ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
        if (complement == null)
            searchCriteria.setName(label);
        else
            searchCriteria.setQ(label + " " + complement);
        searchCriteria.setMaxRows(1);
        if (featureCode != null) searchCriteria.setFeatureCode(featureCode);

        if ("RGN".equals(featureCode)) try {
            searchCriteria.setCountryCode("fr");
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        }

        try {
            ToponymSearchResult searchResult = WebService.search(searchCriteria);
            if (searchResult.getToponyms().size() > 0)
                tp = searchResult.getToponyms().get(0);

            addToCache(id, tp != null ? tp.getGeoNameId() : -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tp;


    }

    // Use cache for passing from ITEMA3 id to GeoNames id
    public static int get(String id) {
        return cache.getOrDefault(id, -1);
    }

    static void loadCache() {
        cache = new HashMap<>();
        try {
            FileInputStream fis = new FileInputStream("places.properties");
            Properties properties = new Properties();
            properties.load(fis);

            for (String key : properties.stringPropertyNames()) {
                cache.put(key, Integer.parseInt(properties.get(key).toString()));
            }
        } catch (IOException e) {
            System.out.println("No 'places.properties' file found. I will create it.");
        }

    }

    private static void addToCache(String key, int value) {
        cache.put(key, value);
        saveCache();
    }

    static void removeFromCache(String key) {
        cache.remove(key);
        saveCache();
    }

    private static void saveCache() {
        Properties properties = new Properties();

        for (Map.Entry<String, Integer> entry : cache.entrySet()) {
            properties.put(entry.getKey(), entry.getValue() + "");
        }

        try {
            properties.store(new FileOutputStream("places.properties"), null);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}