package de.dpunkt.myaktion.test;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.dpunkt.myaktion.controller.GeldSpendenController;
import de.dpunkt.myaktion.controller.SpendeListController;
import de.dpunkt.myaktion.model.Aktion;
import de.dpunkt.myaktion.model.Konto;
import de.dpunkt.myaktion.model.Spende;
import de.dpunkt.myaktion.services.AktionService;
import de.dpunkt.myaktion.services.AktionServiceBean;
import de.dpunkt.myaktion.services.SpendeService;
import de.dpunkt.myaktion.services.MockAktionServiceBean;
import de.dpunkt.myaktion.services.SpendeServiceBean;
import de.dpunkt.myaktion.util.Resources;

@RunWith(Arquillian.class)
public class SpendeListTest {
   @Deployment
   public static Archive<?> createTestArchive() {
      return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClasses(Aktion.class, Konto.class, Spende.class, 
            		SpendeListController.class, GeldSpendenController.class, SpendeService.class, SpendeServiceBean.class,
            		AktionService.class, AktionServiceBean.class, Resources.class, MockAktionServiceBean.class)
            .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsWebInfResource("test-ds.xml", "test-ds.xml");
   }

   @Inject
   private SpendeListController spendeListController;
   
   @Inject
   private GeldSpendenController geldSpendenController;
   
   @Inject
   private SpendeService spendeService;
   
   @Inject
   private AktionService aktionService;

   @Test
   public void testAddSpende() throws Exception {
	   Aktion aktion = MockAktionServiceBean.createMockAktion();
	   aktionService.addAktion(aktion);
	   Long aktionId = aktion.getId();
	   geldSpendenController.setAktionId(aktionId);
	   geldSpendenController.setSpende(MockAktionServiceBean.createMockSpende());
	   // Besser geldSpendenController.doSpende() benutzen - benötigt allerdings gemockten FacesContext 
	   spendeService.addSpende(aktionId, geldSpendenController.getSpende());
	   Long spendeId = geldSpendenController.getSpende().getId();
	   spendeListController.setAktion(aktion);
	   List<Spende> spenden = spendeListController.getSpenden();
	   Assert.assertNotNull(spenden);
	   Assert.assertEquals(spendeId, spenden.get(0).getId());
   }
   
   @Test
   public void testBisherGespendet() throws Exception {
	   Aktion aktion = MockAktionServiceBean.createMockAktion();
	   aktionService.addAktion(aktion);	   
	   // zwei Spenden erstellen (pro Spende 20,-)
	   spendeService.addSpende(aktion.getId(), MockAktionServiceBean.createMockSpende());
	   spendeService.addSpende(aktion.getId(), MockAktionServiceBean.createMockSpende());
	   List<Aktion> aktionen = aktionService.getAllAktionen();
	   Aktion managedAktion = null;
	   for(Aktion a: aktionen) {
		   if(a.getId()==aktion.getId())
			   managedAktion = a;
	   }
	   Double bisherGespendet = managedAktion.getBisherGespendet();
	   Assert.assertEquals(new Double(40d), bisherGespendet);
   }

}