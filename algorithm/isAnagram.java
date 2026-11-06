import java.util.HashMap;
import java.util.Map;

/**
 * 字母异位词练习：判断两个字符串的各字符出现次数是否相同，顺序无关。
 * 保留HashMap和26格数组两种计数实现；前者按Java char计数，后者限于小写英文字母。
 * 两种实现都要求非null，不进行Unicode码点归一化或大小写转换。
 */
public class isAnagram {
    /**
     * 打印异位词、不同词、计数不同、长度不同和空字符串示例；入口调用的是HashMap版本。
     */
    public static void main(String[] args) {
        System.out.println(isAnagram("anagram", "nagaram"));
        System.out.println(isAnagram("rat", "car"));
        System.out.println(isAnagram("aab", "abb"));
        System.out.println(isAnagram("aab", "ab"));
        System.out.println(isAnagram("", ""));
    }

    /**
     * 先比较长度，统计s中各char次数，再遍历t逐个消耗，不存在或已耗尽则失败。
     * 等长保证成功消耗t后不会留下剩余次数；平均时间O(n)、额外空间O(u)，u为不同char数量。
     */
    public static boolean isAnagram(String s, String t) {
        if(s.length() != t.length()){
            return false;
        }
        Map<Character, Integer> map = new HashMap<>();
        for (char c : s.toCharArray()) {
            map.put(c, map.getOrDefault(c, 0) + 1);
        }
        for (char c : t.toCharArray()) {
            if (!map.containsKey(c)) {
                return false;
            }
            if (map.get(c) == 0) {
                return false;
            }
            map.put(c, map.get(c) - 1);
        }
        return true;
    }

    /**
     * 限定输入仅含小写a到z；等长时同轮对s字符加1、t字符减1，全部26项为0才代表计数相同。
     * 时间O(n+26)、额外空间O(26)；不满足字符范围会导致数组越界。
     */
    public static boolean isAnagram2(String s, String t) {
        if(s.length() != t.length()){
            return false;
        }
        int[] count = new int[26];
        for (int i = 0; i < s.length(); i++) {
            count[s.charAt(i) - 'a']++;
            count[t.charAt(i) - 'a']--;
        }
        for (int i = 0; i < 26; i++) {
            if (count[i] != 0) {
                return false;
            }
        }
        return true;
    }


}
