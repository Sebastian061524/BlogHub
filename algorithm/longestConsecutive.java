import java.util.HashSet;
public class longestConsecutive {
    public static void main(String[] args) {
        int[] nums = new int[]{100, 4, 200, 1, 3, 2};
        System.out.println(longestConsecutive(nums));
    }
    public static int longestConsecutive(int[] nums) {
        if(nums.length == 0){
            return 0;
        }
        HashSet<Integer> set = new HashSet<>();
        int maxlength = 0;
        int current = 0;
        for (int num : nums) {
            set.add(num);
        }
        int length = 0;
        for(int num : set){
            if(!set.contains(num - 1)){
                current = num;
                length = 1;
                while (set.contains(current + 1)) {
                    current++;
                    length++;
                }
                maxlength = Math.max(maxlength, length);
            }
        }
        return maxlength;
    }
}
