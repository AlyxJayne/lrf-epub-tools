package lrf.html;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import lrf.objects.tags.Tag;

public class HtmlStyle {
	
	Hashtable<String,StyleItem> vitems=new Hashtable<String,StyleItem>();
	
	private HtmlStyle(Hashtable<String, StyleItem> vi){
		vitems=vi;
	}
	
	public int getNumProps(){
		return vitems.size();
	}
	
	public HtmlStyle(Vector<Tag> tgs){
		addTags(tgs);
	}

	@SuppressWarnings("unchecked")
	public HtmlStyle(HtmlStyle cp) throws CloneNotSupportedException{
		vitems=(Hashtable<String, StyleItem>)cp.vitems.clone();
	}
	
	public HtmlStyle(StyleItem si){
		vitems.put(si.propName,si);
	}
	
	private void addTags(Vector<Tag> tgs) {
		for(int i=0;i<tgs.size();i++){
			Tag t=tgs.elementAt(i);
			addTag(t,false);
		}
	}

	public void addTag(Tag t,boolean recalc) {
		StyleItem si=StyleItem.translate(t);
		if(si!=null)
			vitems.put(si.propName,si);
	}

	public String toString(){
		return "{"+getStyleContent(StyleItem.st_all)+"}";
	}
	
	public String getStyleContent(int level){
		String ret="";
		String tosort[]=new String[vitems.size()];
		int i=0;
		for(Enumeration<String> esi=vitems.keys();esi.hasMoreElements();){
			tosort[i++]=esi.nextElement();
		}
		Arrays.sort(tosort);
		for(i=0;i<tosort.length;i++){
			StyleItem si=vitems.get(tosort[i]);
			if((level & si.getLevel())!=0){
				if(ret.length()>0)
					ret+="; ";
				ret+=vitems.get(tosort[i]).getExpression();
				}
		}
		return ret;
	}
	
	public void overrideWith(HtmlStyle tgs){
		vitems.putAll(tgs.vitems);
	}
	
	/**
	 * Devuelve un HtmlStyle con las props que estan en 'other' y no en este HtmlStyle
	 * @param other
	 * @param level
	 * @return
	 */
	public HtmlStyle newStyles(HtmlStyle other, int level){
		Hashtable<String,StyleItem> ret=new Hashtable<String, StyleItem>();
		for(Enumeration<String> oks=other.vitems.keys();oks.hasMoreElements();){
			String ok=oks.nextElement();
			StyleItem osi=other.vitems.get(ok);
			if((osi.getLevel() & level) ==0)
				continue;
			if(vitems.get(ok)==null){
				ret.put(ok,other.vitems.get(ok));
			}else if(!vitems.get(ok).getExpression().equals(other.vitems.get(ok).getExpression())){
				ret.put(ok,other.vitems.get(ok));
			}
		}
		return new HtmlStyle(ret);
	}
}
