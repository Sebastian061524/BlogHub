import java.util.Arrays;

public class moveZeroes {
    public static void main(String[] args) {
        checkMoveZeroes("Mixed values", new int[] {0, 1, 0, 3, 12}, new int[] {1, 3, 12, 0, 0});
        checkMoveZeroes("All zeroes", new int[] {0, 0}, new int[] {0, 0});
        checkMoveZeroes("No zeroes", new int[] {1, -2, 3}, new int[] {1, -2, 3});
        checkMoveZeroes("Empty array", new int[] {}, new int[] {});
    }

    private static void checkMoveZeroes(String testName, int[] input, int[] expected) {
        moveZeroes(input);
        if (!Arrays.equals(input, expected)) {
            throw new AssertionError(testName + ": expected " + Arrays.toString(expected)
                    + ", got " + Arrays.toString(input));
        }
        System.out.println(testName + ": passed");
    }

    public static void moveZeroes(int[] nums){
        int write = 0;
        int n = nums.length;
        for(int i = 0; i < n; i++){
            if(nums[i] != 0){
                nums[write] = nums[i];
                write++;
            }
        }
        for(int i = write; i < n; i++){
            nums[i] = 0;
        }
    }
}
