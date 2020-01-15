package com.hewgill.android.nzsldict;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Dictionary {
    private static volatile Dictionary instance = null;

    public static class DictCategory implements Serializable, Comparable<DictCategory> {
        public String name;
        public ArrayList<DictItem> words;

        public DictCategory(String name, ArrayList<DictItem> words) {
            this.name = name;
            this.words = words;
        }

        @Override
        public int compareTo(@NonNull DictCategory other) {
            return this.name.compareTo(other.name);
        }
    }

    public static class DictItem implements Serializable, Comparable<DictItem> {
        public String gloss;
        public String minor;
        public String maori;
        public String normGloss;
        public String normMinor;
        public String normMaori;
        public ArrayList<String> glossWords;
        public ArrayList<String> minorWords;
        public ArrayList<String> maoriWords;
        public String image;
        public String video;
        public String handshape;
        public String location;
        public ArrayList<String> categories;

        static Map<String, String> Locations = new HashMap<String, String>();

        static {
            Locations.put("in front of body", "location_1_1_in_front_of_body");
            //"palm",
            Locations.put("chest", "location_4_12_chest");
            Locations.put("lower head", "location_3_9_lower_head");
            Locations.put("fingers/thumb", "location_6_20_fingers_thumb");
            Locations.put("in front of face", "location_2_2_in_front_of_face");
            Locations.put("top of head", "location_3_4_top_of_head");
            Locations.put("head", "location_3_3_head");
            Locations.put("cheek", "location_3_8_cheek");
            Locations.put("nose", "location_3_6_nose");
            Locations.put("back of hand", "location_6_22_back_of_hand");
            Locations.put("neck/throat", "location_4_10_neck_throat");
            Locations.put("shoulders", "location_4_11_shoulders");
            //"blades",
            Locations.put("abdomen", "location_4_13_abdomen");
            Locations.put("eyes", "location_3_5_eyes");
            Locations.put("ear", "location_3_7_ear");
            Locations.put("hips/pelvis/groin", "location_4_14_hips_pelvis_groin");
            Locations.put("wrist", "location_6_19_wrist");
            Locations.put("lower arm", "location_5_18_lower_arm");
            Locations.put("upper arm", "location_5_16_upper_arm");
            Locations.put("elbow", "location_5_17_elbow");
            Locations.put("upper leg", "location_4_15_upper_leg");
        }

        public DictItem() {
            gloss = null;
            minor = null;
            maori = null;
            normGloss = null;
            normMinor = null;
            normMaori = null;
            glossWords = null;
            minorWords = null;
            maoriWords = null;
            image = null;
            video = null;
            handshape = null;
            location = null;
            categories = null;
        }

        public DictItem(String gloss, String minor, String maori, String image, String video,
                        String handshape, String location, String categories, String normGloss,
                        String glossWords, String normMinor, String minorWords, String normMaori, String maoriWords) {
            this.gloss = gloss;
            this.minor = minor;
            this.maori = maori;
            this.normGloss = normGloss;
            this.normMinor = normMinor;
            this.normMaori = normMaori;
            this.glossWords = new ArrayList<>(Arrays.asList(glossWords.split("\\|")));
            this.minorWords = new ArrayList<>(Arrays.asList(minorWords.split("\\|")));
            this.maoriWords = new ArrayList<>(Arrays.asList(maoriWords.split("\\|")));
            this.categories = new ArrayList<>(Arrays.asList(categories.split("\\|")));
            this.image = image;
            this.video = video;
            this.handshape = handshape;
            this.location = location;
        }

        public String imagePath() {
            if (image.isEmpty()) return "";
            return "images/signs/" + image.toLowerCase();
        }

        public String handshapeImage() {
            return "handshape_" + handshape.replaceAll("[.]", "_");
        }

        public String locationImage() {
            String r = Locations.get(location);
            if (r == null) {
                r = "";
            }
            return r;
        }

        public String toString() {
            return gloss + "|" + minor + "|" + maori;
        }

        @Override
        public int compareTo(@NonNull DictItem other) {
            return skipParens(this.gloss).compareToIgnoreCase(skipParens(other.gloss));
        }
    }

    private ArrayList<DictItem> words = new ArrayList<>();
    private Map<String, DictCategory> categories = new HashMap<>();

    public static Dictionary getInstance(Context context) {
        if (instance == null) {
            synchronized(Dictionary.class) {
                if (instance == null) {
                    instance = new Dictionary(context);
                }
            }
        }
        return instance;
    }

    private Dictionary(Context context) {
        InputStream db = null;
        try {
            db = context.getAssets().open("db/nzsl.dat");
            BufferedReader f = new BufferedReader(new InputStreamReader(db));
            while (true) {
                String s = f.readLine();
                if (s == null) {
                    break;
                }
                String[] a = s.split("\t", -1);
                DictItem item = new DictItem(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8], a[9], a[10], a[11], a[12], a[13]);
                words.add(item);
            }
        } catch (IOException x) {
            Log.d("dictionary", "exception reading from word list " + x.getMessage());
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (IOException x) {
                    // ignore
                }
            }
        }
        Collections.sort(words);

        for (DictItem item : words) {
            // give each tag a reference to the item
            for (String category : item.categories) {
                if (!categories.containsKey(category)) {
                    categories.put(category, new DictCategory(category, new ArrayList<DictItem>()));
                }
                categories.get(category).words.add(item);
            }
        }
    }

    public ArrayList<DictItem> getWords() {
        return words;
    }

    public Map<String, DictCategory> getCategories() {
        return categories;
    }

    private static String normalise(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD).toLowerCase();
        StringBuilder r = new StringBuilder(s.length());
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            // 0-9, a-z
            if (48 <= c && c <= 57 || 97 <= c && c <= 122) {
                r.append(c);
            }
            // <space>, <comma>
            else if (c == 32 || c == 44) {
                r.append(c);
            }
        }
        return r.toString();
    }

    public ArrayList<DictItem> getWords(String target) {
        // Create a sorted set for each type of match. This provides "buckets" to place results
        // in. Because it is a sorted set, uniqueness is guaranteed, and results should also be
        // naturally ordered.
        SortedSet<DictItem> exactPrimaryMatches = new TreeSet<>();
        SortedSet<DictItem> exactPrimaryWordMatches = new TreeSet<>();
        SortedSet<DictItem> exactSecondaryMatches = new TreeSet<>();
        SortedSet<DictItem> exactSecondaryWordMatches = new TreeSet<>();
        SortedSet<DictItem> startsWithPrimaryMatches = new TreeSet<>();
        SortedSet<DictItem> containsPrimaryMatches = new TreeSet<>();
        SortedSet<DictItem> containsSecondaryMatches = new TreeSet<>();

        String term = normalise(target);

        for (DictItem d : words) {
            if (d.normGloss.equals(term) || d.normMaori.equals(term)) exactPrimaryMatches.add(d);
            else if (d.normMinor.equals(term)) exactSecondaryMatches.add(d);
            else if (d.glossWords.contains(term) || d.maoriWords.contains(term)) exactPrimaryWordMatches.add(d);
            else if (d.minorWords.contains(term)) exactSecondaryWordMatches.add(d);
            else if (d.normGloss.startsWith(term) || d.normMaori.startsWith(term)) startsWithPrimaryMatches.add(d);
            else if (d.normGloss.contains(term) || d.normMaori.contains(term)) containsPrimaryMatches.add(d);
            else if (d.normMinor.contains(term)) containsSecondaryMatches.add(d);
        }

        // Create an ArrayList of the size of all the results, and add each of the sets to it.
        // This returns a data structure ordered first by the priority of the result type, then
        // natural ordering within each set, e.g.:
        // Given: [exact: [e1, e2, e3], contains: [c1, c2, c2], exactSecondary: [es1, es2, es3]
        // Then: results = [e1, e2, e3, c1, c2, c3, es1, es2, es3]
        int resultCount = exactPrimaryMatches.size() +
                exactSecondaryMatches.size() +
                exactPrimaryWordMatches.size() +
                exactSecondaryWordMatches.size() +
                startsWithPrimaryMatches.size() +
                containsPrimaryMatches.size() +
                containsSecondaryMatches.size();

        LinkedHashSet<DictItem> results = new LinkedHashSet<>(resultCount);
        results.addAll(exactPrimaryMatches);
        results.addAll(exactSecondaryMatches);
        results.addAll(exactPrimaryWordMatches);
        results.addAll(exactSecondaryWordMatches);
        results.addAll(startsWithPrimaryMatches);
        results.addAll(containsPrimaryMatches);
        results.addAll(containsSecondaryMatches);

        return new ArrayList<>(results);
    }

    public List<DictItem> getWordsByHandshape(String handshape, String location) {
        List<DictItem> r = new ArrayList<>(words.size());
        for (DictItem d : words) {
            if ((handshape == null || handshape.length() == 0 || d.handshape.equals(handshape))
                    && (location == null || location.length() == 0 || d.location.equals(location))) {
                r.add(d);
            }
        }
        return r;
    }

    private static Set taboo = new HashSet<>(Arrays.asList(
        "(vaginal) discharge",
        "abortion",
        "abuse",
        "anus",
        "asshole",
        "attracted",
        "balls",
        "been to",
        "bisexual",
        "bitch",
        "blow job",
        "breech (birth)",
        "bugger",
        "bullshit",
        "cervical smear",
        "cervix",
        "circumcise",
        "cold (behaviour)",
        "come",
        "condom",
        "contraction (labour)",
        "cunnilingus",
        "cunt",
        "damn",
        "defecate, faeces",
        "dickhead",
        "dilate (cervix)",
        "ejaculate, sperm",
        "episiotomy",
        "erection",
        "fart",
        "foreplay",
        "gay",
        "gender",
        "get intimate",
        "get stuffed",
        "hard-on",
        "have sex",
        "heterosexual",
        "homo",
        "horny",
        "hysterectomy",
        "intercourse",
        "labour pains",
        "lesbian",
        "lose one's virginity",
        "love bite",
        "lust",
        "masturbate (female)",
        "masturbate, wanker",
        "miscarriage",
        "orgasm",
        "ovaries",
        "penis",
        "period",
        "period pains",
        "promiscuous",
        "prostitute",
        "rape",
        "sanitary pad",
        "sex",
        "sexual abuse",
        "shit",
        "smooch",
        "sperm",
        "strip",
        "suicide",
        "tampon",
        "testicles",
        "vagina",
        "virgin"
    ));

    public DictItem getWordOfTheDay() {
        String buf = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(buf.getBytes());
            int r = (((digest[0] & 0xff) << 8) | (digest[1] & 0xff)) % words.size();
            while (taboo.contains(words.get(r).gloss)) {
                r += 1;
            }
            return words.get(r);
        } catch (java.security.NoSuchAlgorithmException x) {
            // shouldn't happen, but return something sensible if it does
            return words.get(new java.util.Random().nextInt(words.size()));
        }
    }

    private static String skipParens(String s) {
        if (s.charAt(0) == '(') {
            int i = s.indexOf(") ");
            if (i > 0) {
                s = s.substring(i + 2);
            } else {
                Log.d("Dictionary", "expected to find closing parenthesis: " + s);
            }
        }
        return s;
    }
}
