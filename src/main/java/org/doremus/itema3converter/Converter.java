package org.doremus.itema3converter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {

    public static final String UTF8_BOM = "\uFEFF";

    public static Properties properties;
    private static String dataFolderPath;

    public static void main(String[] args) throws IOException, XMLStreamException, TransformerException {
        // INIT
        System.out.println("\n\n******* Running ITEMA3 Converter *********");

        loadProperties();
        System.out.println(properties);

        System.out.println("\n\n");
        // end INIT

        File inputFolder = new File(properties.getProperty("src"));
        dataFolderPath = Paths.get(properties.getProperty("src"), "data").toString();
        File dataFolder = new File(dataFolderPath);

        if (!dataFolder.exists()) {
            System.out.println("Pre-processing data");
            for (File f : inputFolder.listFiles())
                fileToFolder(f);
            System.out.println("End Pre-processing\n\n");
        }

        // TODO continue

    }

    private static void fileToFolder(File f) throws XMLStreamException, IOException, TransformerException {
        // Separate the long files in a folder with a file for each record
        if (!f.getName().endsWith(".xml")) return;
        removeUTF8BOM(f);

        String fileName = f.getName().replaceFirst("\\.xml", "");
//        if (!fileName.equals("TH_VA_MORALE")) return;
        System.out.println(fileName);


        new File(Paths.get(dataFolderPath, fileName).toString()).mkdirs();

        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(f));
        xsr.next(); // Skip Doctype
        xsr.nextTag(); // Advance to statements element

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        QName recordName = QName.valueOf("DATA_RECORD");

        String idMain = fileName.replaceFirst("TH_VA_", "");
        if (idMain.equals("NC")) idMain = "NOM_COMMUN"; // known abbreviation
        Pattern p = Pattern.compile("<" + idMain + "_ID>(.+)<\\/" + idMain + "_ID>");

        while (xsr.hasNext()) {
            if (xsr.next() != XMLStreamConstants.START_ELEMENT || !xsr.getName().equals(recordName)) continue;
            StringWriter stringWriter = new StringWriter();

            t.transform(new StAXSource(xsr), new StreamResult(stringWriter));
            String recordString = stringWriter.toString();
//            System.out.println(recordString);
            Matcher m = p.matcher(recordString);
            String id;
            if (m.find()) {
                id = m.group(1);
            } else {
                // it is a join table
                String splitter = "_";
                for (String code : new String[]{"_IDX_", "_TH_"})
                    if (fileName.contains(code)) splitter = code;

                String[] parts = fileName
                        .replaceFirst("TH_LIEN_", "")
                        .split(splitter, 2);

                for (int i = 0; i < parts.length; i++) {
                    // System.out.println("> " + parts[i]);
                    if (parts[i].equals("NC")) parts[i] = "NOM_COMMUN"; // known abbreviation
                }

                // workaround for this exception
                if (fileName.equals("OMU_PERSONNE_STATION"))
                    parts = new String[]{"OMU_PERSONNE", "STATION"};
                if (fileName.startsWith("TH_DOMAINE"))
                    parts = new String[]{"TH_DOMAINE", fileName.replace("TH_DOMAINE_", "")};

                Pattern p1 = Pattern.compile("<" + parts[0] + "_ID>(.+)<\\/" + parts[0] + "_ID>");
                Pattern p2 = Pattern.compile("<" + parts[1] + "_ID>(.+)<\\/" + parts[1] + "_ID>");

                Matcher m1 = p1.matcher(recordString);
                Matcher m2 = p2.matcher(recordString);
                m1.find();
                m2.find();

                id = m1.group(1) + "_" + m2.group(1);
            }

            Files.write(Paths.get(dataFolderPath, fileName, id + ".xml"), recordString.getBytes(),
                    StandardOpenOption.CREATE);


//                    File file = new File("out/" + xsr.getAttributeValue(null, "account") + ".xml");
//                t.transform(new StAXSource(xsr), new StreamResult(file));
        }
        xsr.close();
    }


    private static void removeUTF8BOM(File f) throws IOException {
        // remove UTF8 BOM
        // https://stackoverflow.com/questions/4569123/content-is-not-allowed-in-prolog-saxparserexception
        boolean firstLine = true;
        FileInputStream fis = new FileInputStream(f);
        BufferedReader r = new BufferedReader(new InputStreamReader(fis, "UTF8"));

        StringBuilder sb = new StringBuilder();
        for (String s; (s = r.readLine()) != null; ) {
            if (firstLine) {
                if (s.startsWith(UTF8_BOM)) s = s.substring(1);
                firstLine = false;
            } else sb.append("\n");
            sb.append(s);
        }
        r.close();
        Files.delete(f.toPath());

        String output = sb.toString().replaceAll("&#(31|28|29);", " ")
                .replaceAll("&#30;", "f");

        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF8"));
        w.write(output);
        w.close();
    }

    private static void loadProperties() {
        properties = new Properties();
        String filename = "config.properties";

        try {
            InputStream input = new FileInputStream(filename);
            properties.load(input);
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}


