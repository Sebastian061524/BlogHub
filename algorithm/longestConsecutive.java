import java.util.HashSet;
/**
 * 最长连续整数序列练习：输入无序数组，返回数值连续序列的最大长度，与原位置无关。
 * HashSet先去重，再仅从没有前驱的起点向后扫描，每个不同数最多参与一次扩展。
 * 平均时间O(n)、额外空间O(n)；去重和只从起点扫描是避免重复计数及二次复杂度的关键。
 * 约定元素范围[-10^9, 10^9]，num-1与current+1不会int溢出；当前未处理范围外的溢出语义。
 */
public class longestConsecutive {
    /**
     * 打印无序样例的最长连续长度，其中1、2、3、4构成长度4的连续序列。
     */
    public static void main(String[] args) {
        int[] nums = new int[]{100, 4, 200, 1, 3, 2};
        System.out.println(longestConsecutive(nums));
    }
    /**
     * 空数组返回0；集合去重后，仅当num-1不存在才从num向后扩展，并维护最大长度。
     * 每段序列只有一个起点，因此不会从中间反复扫描；nums须非null且元素遵守类注释中的范围。
     */
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
