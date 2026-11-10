public class mergeTwoLists {

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    public static void main(String[] args){
        checkMerged("Interleaved lists", list(1, 3, 5), list(2, 4, 6), 1, 2, 3, 4, 5, 6);
        checkMerged("Duplicate values", list(1, 2), list(1, 3), 1, 1, 2, 3);
        checkMerged("Empty left list", null, list(2, 4), 2, 4);
        checkMerged("Two empty lists", null, null);
    }

    private static ListNode list(int... values) {
        ListNode head = null;
        for (int i = values.length - 1; i >= 0; i--) {
            ListNode node = new ListNode(values[i]);
            node.next = head;
            head = node;
        }
        return head;
    }

    private static void checkMerged(String testName, ListNode left, ListNode right, int... expected) {
        ListNode node = MergeTwoLists(left, right);
        for (int value : expected) {
            if (node == null || node.val != value) {
                throw new AssertionError(testName + ": expected " + value
                        + ", got " + (node == null ? "end of list" : node.val));
            }
            node = node.next;
        }
        if (node != null) {
            throw new AssertionError(testName + ": unexpected extra node " + node.val);
        }
        System.out.println(testName + ": PASS");
    }

    public static ListNode MergeTwoLists(ListNode left, ListNode right){
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        while(left != null && right != null){
            if(left.val < right.val){
                tail.next = left;
                left = left.next;
            }
            else{
                tail.next = right;
                right = right.next;
            }
            tail = tail.next;
        }
        tail.next = left != null ? left : right;
        return dummy.next;
    }
}
