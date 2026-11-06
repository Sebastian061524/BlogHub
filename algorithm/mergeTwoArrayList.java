/**
 * 有序数组原地合并练习：将nums2前n个数并入nums1的前m个有效数，结果写回nums1。
 * 虽然类名保留ArrayList字样，实际操作int[]；nums1须至少有m+n个位置。
 * 前提是两个有效区间均非递减有序，数组非null且m、n与容量合法。
 */
public class mergeTwoArrayList {
    /**
     * 准备nums1末尾有3个空位的有序样例，合并后逐行打印；末尾0是容量占位，不属于初始有效数据。
     */
    public static void main(String[] args) {
        int[] nums1 = new int[]{1, 2, 3, 0, 0, 0};
        int[] nums2 = new int[]{2, 5, 6};
        merge(nums1, 3, nums2, 3);
        for (int i = 0; i < nums1.length; i++) {
            System.out.println(nums1[i]);
        }

    }

    /**
     * i、j指向两个有效区间末尾，k指向待填结果最右位置，每次选较大值从后向前放置。
     * 右侧始终放好最大的一批值，从尾部写入避免覆盖nums1尚未读取的有效数据。
     * nums2有剩余须复制，nums1有剩余已在正确位置无需搬动；时间O(m+n)、额外空间O(1)。
     */
    public static void merge(int[] nums1, int m, int[] nums2, int n) {
        int i = m - 1;
        int j = n - 1;
        int k = m + n - 1;
        while (i >= 0 && j >= 0) {
            if(nums1[i] > nums2[j]){
                nums1[k] = nums1[i];
                i--;
            }else{
                nums1[k] = nums2[j];
                j--;
            }
            k--;
        }
        while (j >= 0) {
            nums1[k] = nums2[j];
            j--;
            k--;
        }
    }
}
