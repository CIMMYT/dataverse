package edu.harvard.iq.dataverse;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author CIMMYT
 */


@Entity
public class DataverseControlledVocabulary implements  Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean isimport = false;

    public Boolean getIsimport() {
        return isimport;
    }

    public void setIsimport(Boolean isimport) {
        this.isimport = isimport;
    }


    @ManyToOne
    @JoinColumn(name = "dataverse_id")
    private Dataverse dataverse;

    @ManyToOne
    @JoinColumn(name = "ControlledVocabulary_id")
    private ControlledVocabulary controlledVocabulary;

    public DataverseControlledVocabulary(){

    }
    public DataverseControlledVocabulary(Dataverse dv, ControlledVocabulary cv){
        this.dataverse = dv;
        this.controlledVocabulary = cv;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Dataverse getDataverse() {
        return dataverse;
    }

    public void setDataverse(Dataverse dataverse) {
        this.dataverse = dataverse;
    }

    public ControlledVocabulary getControlledVocabulary() {
        return controlledVocabulary;
    }

    public void setControlledVocabulary(ControlledVocabulary controlledVocabulary) {
        this.controlledVocabulary = controlledVocabulary;
    }

}
