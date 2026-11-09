
public class reverseList {
    public static void main(String[] args){
        ListNode a = new ListNode(1);
        ListNode b = new ListNode(2);
        ListNode c = new ListNode(3);
        a.next = b;
        b.next = c;
        c.next = null;
        ListNode d = ReverseList(a);
        for (ListNode node = d; node != null; node = node.next) {
            System.out.println(node.val);
        }

        ListNode e = new ListNode(2);
        d = ReverseList(e);
        for (ListNode node = d; node != null; node = node.next) {
            System.out.println(node.val);
        }
        System.out.println(d == e && d.next == null);
        ListNode f = null;
        d = ReverseList(f);
        System.out.println(ReverseList(null) == null);
    }

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    public static ListNode ReverseList(ListNode head){
        ListNode prev = null;
        ListNode curr = head;

        while(curr != null){
            ListNode tmp = curr.next;
            curr.next = prev;
            prev = curr;
            curr = tmp;
        }

        return prev;
    }
}
