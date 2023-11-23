package tomato.realmshark;

import org.xml.sax.SAXException;
import util.StringXML;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * HTTP Requests character data from realm servers and converts data to character info.
 */
public class HttpCharListRequest {

    /**
     * Requests character list data from realm servers using the current access token of the logged in user.
     *
     * @param accessToken Access token of the currently logged in user.
     * @return Char list data as XML string.
     */
    public static String getChartList(String accessToken) throws IOException {
        String encoded = URLEncoder.encode(accessToken, "UTF-8");
        String s1 = "https://www.realmofthemadgod.com/char/list?";
        String s2 = "do_login=true&accessToken=" + encoded + "&game_net=Unity&play_platform=Unity&game_net_user_id";

        URL obj = new URL(s1 + s2);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        con.setDoOutput(true);
        con.setRequestMethod("POST");

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = s2.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
//            System.out.println(response);
            return response.toString();
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            in.close();
            System.out.println(response);
        }
        return null;
    }

    /**
     * Converts XML data to array of Character data.
     *
     * @param r XML string to be parsed.
     * @return List of Character data parsed from the XML string.
     */
    public static ArrayList<RealmCharacter> getCharList(String r) {
//        prettyXML(r);

        StringXML base;
        ArrayList<RealmCharacter> listChars = new ArrayList<>();

//        if (r == null) {
//            r = "";
//            try {
//                BufferedReader br = new BufferedReader(new InputStreamReader(Util.resourceFilePath("temp"), StandardCharsets.UTF_8));
//                String line;
//                while ((line = br.readLine()) != null) {
//                    r += line;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//        }

        try {
            base = StringXML.getParsedXML(r);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }

        for (StringXML xml : base) {
            if (Objects.equals(xml.name, "Char")) {
                RealmCharacter character = new RealmCharacter();
                for (StringXML info : xml) {
                    if (Objects.equals(info.name, "id")) {
                        character.charId = Integer.parseInt(info.value);
                    }

                    for (StringXML v : info) {
                        switch (info.name) {
                            case "ObjectType":
                                character.classNum = Short.parseShort(v.value);
                                character.setClassString();
                                break;
                            case "Equipment":
                                character.equipment = Arrays.stream(v.value.split(",")).mapToInt(Integer::parseInt).toArray();
                                break;
                            case "EquipQS":
                                character.equipQS = v.value.split(",");
                                break;
                            case "Level":
                                character.level = Integer.parseInt(v.value);
                                break;
                            case "Texture":
                                character.skin = Integer.parseInt(v.value);
                                break;
                            case "CreationDate":
                                character.date = v.value;
                                break;
                            case "HasBackpack":
                                character.backpack = v.value.equals("1");
                                break;
                            case "Has3Quickslots":
                                character.qs3 = v.value.equals("1");
                                break;
                            case "MaxHitPoints":
                                character.hp = Integer.parseInt(v.value);
                                break;
                            case "MaxMagicPoints":
                                character.mp = Integer.parseInt(v.value);
                                break;
                            case "Attack":
                                character.atk = Integer.parseInt(v.value);
                                break;
                            case "Defense":
                                character.def = Integer.parseInt(v.value);
                                break;
                            case "Speed":
                                character.spd = Integer.parseInt(v.value);
                                break;
                            case "Dexterity":
                                character.dex = Integer.parseInt(v.value);
                                break;
                            case "HpRegen":
                                character.vit = Integer.parseInt(v.value);
                                break;
                            case "MpRegen":
                                character.wis = Integer.parseInt(v.value);
                                break;
                            case "Seasonal":
                                character.seasonal = v.value.equals("True");
                                break;
                            case "Exp":
                                character.exp = Long.parseLong(v.value);
                                break;
                            case "CurrentFame":
                                character.fame = Long.parseLong(v.value);
                                break;
                            case "PCStats":
                                character.pcStats = v.value;
                                character.setCharacterStats();
                                break;
                            case "Pet":
                                for (StringXML pet : info) {
                                    switch (pet.name) {
                                        case "createdOn":
                                            character.petCreatedOn = pet.value;
                                            break;
                                        case "instanceId":
                                            character.petInstanceId = Integer.parseInt(pet.value);
                                            break;
                                        case "maxAbilityPower":
                                            character.petMaxAbilityPower = Integer.parseInt(pet.value);
                                            break;
                                        case "name":
                                            character.petName = pet.value;
                                            break;
                                        case "rarity":
                                            character.petRarity = Integer.parseInt(pet.value);
                                            break;
                                        case "skin":
                                            character.petSkin = Integer.parseInt(pet.value);
                                            break;
                                        case "type":
                                            character.petType = Integer.parseInt(pet.value);
                                            break;
                                        case "Abilities":
                                            int[] a = new int[9];
                                            int i = 0;
                                            for (StringXML abilitys : pet) {
                                                for (StringXML ability : abilitys) {
                                                    a[i++] = Integer.parseInt(ability.value);
                                                }
                                            }
                                            character.petAbilitys = a;
                                    }
                                }
                                break;
                        }
                    }
                }
                listChars.add(character);
            } else if (Objects.equals(xml.name, "PowerUpStats")) {
                for (StringXML info : xml) {
                    if (Objects.equals(info.name, "ClassStats")) {
                        int clazz = 0;
                        int[] exalts = null;
                        for (StringXML c : info) {
                            if (Objects.equals(c.name, "class")) {
                                clazz = Integer.parseInt(c.value);
                            } else if (Objects.equals(c.name, "#text")) {
                                exalts = Arrays.stream(c.value.split(",")).mapToInt(Integer::parseInt).toArray();
                            }
                        }
                        RealmCharacter.exalts.put(clazz, exalts);
                    }
                }
            }
        }
        return listChars;
    }

    /**
     * Pretty prints XML
     *
     * @param input Ugly XML text
     */
    private static void prettyXML(String input) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            String out = xmlOutput.getWriter().toString();
            System.out.println(out);
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }

    /**
     * Test function
     */
    public static void main(String[] args) throws FileNotFoundException {
        FileInputStream file = new FileInputStream("tiles/httpraw");
        String s = new BufferedReader(new InputStreamReader(file)).lines().collect(Collectors.joining("\n"));

        getCharList(s);
    }
}