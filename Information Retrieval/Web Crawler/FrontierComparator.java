import java.util.Comparator;

/**
 * Compares two FrontierItems based on seed, then inlinks, then time created
 * @author Austin
 *
 */
public class FrontierComparator implements Comparator {

	public FrontierComparator() {}
	
	public int compare(Object arg1, Object arg2) {
		FrontierItem url1 = (FrontierItem) arg1;
		FrontierItem url2 = (FrontierItem) arg1;
        if (url1.isSeed()) {
            return -1;
        } else if (url2.isSeed()) {
            return 1;
        }

        int res = url1.getInlinkCount().compareTo(url2.getInlinkCount())
                * -1;

        if (res == 0) {
            res = url1.getTimeCreated().compareTo(url2.getTimeCreated());
        }
        return res;
    }
//	@Override
//	public int compare(Object arg0, Object arg1) {
//		// TODO Auto-generated method stub
//		FrontierItem a = (FrontierItem) arg0;
//		FrontierItem b = (FrontierItem) arg1;
//		// a is greater if its a seed and b isn't
//		if ((a.isSeed()) && (!b.isSeed())) {
//			return 1;
//		} 
//		// b is greater if its a seed and a isn't
//		else if ((!a.isSeed()) && (b.isSeed())) {
//			return -1;
//		} 
//		// Both are(n't) seeds
//		// a is greater if it has more inlinks
//		else if (a.getInlinkCount() > b.getInlinkCount()) {
//			return 1;
//		} 
//		// b is greater if it has more inlinks
//		else if (a.getInlinkCount() < b.getInlinkCount()) {
//			return -1;
//		} 
//		// Both have equal inlinks
//		// a is greater if its timeCreated is lower (was in frontier first)
//		// so b's timecount - a's timecount would have to be positive
//		else {
//			return (int) (b.getTimeCreated() - a.getTimeCreated());
//		}
}


