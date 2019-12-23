package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.search.IndexServiceBean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author CIMMYT
 */
@Stateless
@Named
public class VocabularyServiceBean {

    private static final Logger logger = Logger.getLogger(VocabularyServiceBean.class.getCanonicalName());
    @EJB
    IndexServiceBean indexService;

    @PersistenceContext(unitName = "VDCNet-ejbPU")
    private EntityManager em;


    public DataverseControlledVocabulary saveDataverseControlledVocabulary(DataverseControlledVocabulary dvcv) {
        return em.merge(dvcv);
    }

    public  ControlledVocabulary saveControlledVocabulary(ControlledVocabulary cv){
        return em.merge(cv);
    }

    public ControlledVocabularyTerms saveControlledVocabularyTerms(ControlledVocabularyTerms cvt){
        return em.merge(cvt);
    }

    /*
    public ControlledVocabularyTermsAlternative saveControlledVocabularyTermsAlternative(ControlledVocabularyTermsAlternative cvta){
        return em.merge(cvta);
    }
     */

    public List<ControlledVocabularyValue> getLanguages(){
        return em.createNamedQuery("ControlledVocabularyValue.languageSupport", ControlledVocabularyValue.class).getResultList();
    }

}
