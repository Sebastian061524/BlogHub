import java.util.HashSet;
import java.util.Set;
public class longest{
    public static void main(String[] args) {

    }

    public static int longestConsecutive(int[] nums) {
        if(nums.length == 0){
            return 0;
        }
        Set<Integer> set = new HashSet<>();
        for (int num : nums) {
            set.add(num);
        }
        int maxlength = 0;
        for (int num : set) {
            int current = 0;
            int length = 0;
            if(!set.contains(num - 1)){
                current = num;
                length = 1;
                while(set.contains(current + 1)){
                    current++;
                    length++;
                }
                maxlength = Math.max(maxlength, length);
            }
        }
        return maxlength;
    }
}
