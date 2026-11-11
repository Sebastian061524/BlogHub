public class hasCycle {

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }
    public static void main(String[] args){
        checkCycle("Empty list", null, false);

        ListNode duplicateValues = new ListNode(1);
        duplicateValues.next = new ListNode(2);
        duplicateValues.next.next = new ListNode(2);
        checkCycle("Duplicate values without a cycle", duplicateValues, false);

        ListNode selfCycle = new ListNode(1);
        selfCycle.next = selfCycle;
        checkCycle("Single node pointing to itself", selfCycle, true);

        ListNode first = new ListNode(1);
        ListNode second = new ListNode(2);
        ListNode third = new ListNode(3);
        ListNode fourth = new ListNode(4);
        first.next = second;
        second.next = third;
        third.next = fourth;
        fourth.next = second;
        checkCycle("Tail pointing to the second node", first, true);
    }

    private static void checkCycle(String testName, ListNode head, boolean expected) {
        boolean actual = HasCycle(head);
        if (actual != expected) {
            throw new AssertionError(testName + ": expected " + expected + ", got " + actual);
        }
        System.out.println(testName + ": PASS");
    }

    public static boolean HasCycle(ListNode head){
        ListNode fast = head;
        ListNode slow = head;
        while(fast != null &&  fast.next != null){
            fast = fast.next.next;
            slow = slow.next;
            if(fast == slow){
                return true;
            }
        }
        return false;

    }
}
