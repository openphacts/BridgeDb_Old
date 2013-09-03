// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
//
// Copyright 2006-2009  BridgeDb developers
// Copyright 2012-2013  Christian Y. A. Brenninkmeijer
// Copyright 2012-2013  OpenPhacts
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.bridgedb.ws.bean;

import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import org.bridgedb.statistics.DataSetInfo;
import org.bridgedb.statistics.MappingSetInfo;

/**
 *
 * @author Christian
 */
@XmlRootElement(name="MappingSetInfo")
public class MappingSetInfoBean {
    private Integer id;
    private DataSetInfoBean source;
    private String predicate;
    private DataSetInfoBean target;
    private String justification;
    private Integer symmetric;
    private Integer numberOfLinks;
    private Set<DataSetInfoBean> viaDataSets;
    private Set<Integer> chainId;
    private String mappingName;
    private String mappingUri;    
    private Integer numberOfSources;
    private Integer numberOfTargets;
    private Integer frequencyMedium;
    private Integer frequency75;
    private Integer frequency90;
    private Integer frequency99;
    private Integer frequencyMax;

    /**
     * WS Constructor
     */
    public MappingSetInfoBean(){
    }
    
    public MappingSetInfoBean(MappingSetInfo info) {
        id = info.getIntId();
        source = DataSetInfoBean.asBean(info.getSource());
        predicate = info.getPredicate();
        target = DataSetInfoBean.asBean(info.getTarget());
        justification = info.getJustification();
        mappingName = info.getMappingName();
        mappingUri = info.getMappingUri();
        symmetric = info.getSymmetric();
        viaDataSets = DataSetInfoBean.asBeans(info.getViaDataSets());
        chainId = info.getChainIds();
        numberOfLinks = info.getNumberOfLinks();
        numberOfSources= info.getNumberOfSources();
        numberOfTargets = info.getNumberOfTargets();
        frequencyMedium = info.getFrequencyMedium();
        frequency75 = info.getFrequency75();
        frequency90 = info.getFrequency90();
        frequency99 = info.getFrequency99();
        frequencyMax = info.getFrequencyMax();
    }
    
    public MappingSetInfo asMappingSetInfo(){
       return new MappingSetInfo(getId(), 
               DataSetInfoBean.asDataSetInfo(getSource()), 
               getPredicate(), 
               DataSetInfoBean.asDataSetInfo(getTarget()), 
               getJustification(), 
               getMappingName(),
               getMappingUri(),
               getSymmetric(), 
               DataSetInfoBean.asDataSetInfos(getViaDataSets()), 
               getChainId(), 
               getNumberOfLinks(),
               getNumberOfSources(),
               getNumberOfTargets(),
               getFrequencyMedium(),
               getFrequency75(),
               getFrequency90(),
               getFrequency99(),
               getFrequencyMax());
    }
  
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

   /**
     * @return the predicate
     */
    public String getPredicate() {
        return predicate;
    }

    /**
     * @param predicate the predicate to set
     */
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

     /**
     * @return the numberOfLinks
     */
    public Integer getNumberOfLinks() {
        return numberOfLinks;
    }

    /**
     * @param numberOfLinks the numberOfLinks to set
     */
    public void setNumberOfLinks(Integer numberOfLinks) {
        this.numberOfLinks = numberOfLinks;
    }

    /**
     * @return the justification
     */
    public String getJustification() {
        return justification;
    }

    /**
     * @param justification the justification to set
     */
    public void setJustification(String justification) {
        this.justification = justification;
    }

    /**
     * @return the chainId
     */
    public Set<Integer> getChainId() {
        return chainId;
    }

    /**
     * @param chainId the chainId to set
     */
    public void setChainId(Set<Integer> chainId) {
        this.chainId = chainId;
    }

    /**
     * @return the symmetric
     */
    public Integer getSymmetric() {
        return symmetric;
    }

    /**
     * @param symmetric the symmetric to set
     */
    public void setSymmetric(Integer symmetric) {
        this.symmetric = symmetric;
    }

    /**
     * @return the source
     */
    public DataSetInfoBean getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(DataSetInfoBean source) {
        this.source = source;
    }

    /**
     * @return the target
     */
    public DataSetInfoBean getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(DataSetInfoBean target) {
        this.target = target;
    }

    /**
     * @return the viaDataSets
     */
    public Set<DataSetInfoBean> getViaDataSets() {
        return viaDataSets;
    }

    /**
     * @param viaDataSets the viaDataSets to set
     */
    public void setViaDataSets(Set<DataSetInfoBean> viaDataSets) {
        this.viaDataSets = viaDataSets;
    }

    /**
     * @return the mappingName
     */
    public String getMappingName() {
        return mappingName;
    }

    /**
     * @param mappingName the mappingName to set
     */
    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    /**
     * @return the mappingUri
     */
    public String getMappingUri() {
        return mappingUri;
    }

    /**
     * @param mappingUri the mappingUri to set
     */
    public void setMappingUri(String mappingUri) {
        this.mappingUri = mappingUri;
    }

    /**
     * @return the numberOfSources
     */
    public Integer getNumberOfSources() {
        return numberOfSources;
    }

    /**
     * @param numberOfSources the numberOfSources to set
     */
    public void setNumberOfSources(Integer numberOfSources) {
        this.numberOfSources = numberOfSources;
    }

    /**
     * @return the numberOfTargets
     */
    public Integer getNumberOfTargets() {
        return numberOfTargets;
    }

    /**
     * @param numberOfTargets the numberOfTargets to set
     */
    public void setNumberOfTargets(Integer numberOfTargets) {
        this.numberOfTargets = numberOfTargets;
    }

    /**
     * @return the frequencyMedium
     */
    public Integer getFrequencyMedium() {
        return frequencyMedium;
    }

    /**
     * @param frequencyMedium the frequencyMedium to set
     */
    public void setFrequencyMedium(Integer frequencyMedium) {
        this.frequencyMedium = frequencyMedium;
    }

    /**
     * @return the frequency75
     */
    public Integer getFrequency75() {
        return frequency75;
    }

    /**
     * @param frequency75 the frequency75 to set
     */
    public void setFrequency75(Integer frequency75) {
        this.frequency75 = frequency75;
    }

    /**
     * @return the frequency90
     */
    public Integer getFrequency90() {
        return frequency90;
    }

    /**
     * @param frequency90 the frequency90 to set
     */
    public void setFrequency90(Integer frequency90) {
        this.frequency90 = frequency90;
    }

    /**
     * @return the frequency99
     */
    public Integer getFrequency99() {
        return frequency99;
    }

    /**
     * @param frequency99 the frequency99 to set
     */
    public void setFrequency99(Integer frequency99) {
        this.frequency99 = frequency99;
    }

    /**
     * @return the frequencyMax
     */
    public Integer getFrequencyMax() {
        return frequencyMax;
    }

    /**
     * @param frequencyMax the frequencyMax to set
     */
    public void setFrequencyMax(Integer frequencyMax) {
        this.frequencyMax = frequencyMax;
    }


}
