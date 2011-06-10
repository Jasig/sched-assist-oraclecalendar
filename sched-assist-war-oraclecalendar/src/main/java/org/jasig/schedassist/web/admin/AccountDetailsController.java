/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.jasig.schedassist.web.admin;

import org.apache.commons.lang.StringUtils;
import org.jasig.schedassist.ICalendarAccountDao;
import org.jasig.schedassist.impl.oraclecalendar.OracleGUIDSource;
import org.jasig.schedassist.impl.owner.OwnerDao;
import org.jasig.schedassist.impl.owner.PublicProfileDao;
import org.jasig.schedassist.impl.visitor.NotAVisitorException;
import org.jasig.schedassist.impl.visitor.VisitorDao;
import org.jasig.schedassist.model.ICalendarAccount;
import org.jasig.schedassist.model.IDelegateCalendarAccount;
import org.jasig.schedassist.model.IScheduleOwner;
import org.jasig.schedassist.model.IdentityUtils;
import org.jasig.schedassist.model.PublicProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * {@link Controller} implementation that provides {@link ICalendarAccount} details.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: AccountDetailsController.java 2978 2011-01-25 19:20:51Z npblair $
 */
@Controller
@RequestMapping("/admin/account-details.html")
public class AccountDetailsController {

	private ICalendarAccountDao calendarAccountDao;
	private OracleGUIDSource oracleGUIDSource;
	private VisitorDao visitorDao;
	private OwnerDao ownerDao;
	private PublicProfileDao publicProfileDao;
	/**
	 * @param calendarAccountDao the calendarAccountDao to set
	 */
	@Autowired
	public void setCalendarAccountDao(@Qualifier("composite") ICalendarAccountDao calendarAccountDao) {
		this.calendarAccountDao = calendarAccountDao;
	}
	/**
	 * @param visitorDao the visitorDao to set
	 */
	@Autowired
	public void setVisitorDao(VisitorDao visitorDao) {
		this.visitorDao = visitorDao;
	}
	/**
	 * @param ownerDao the ownerDao to set
	 */
	@Autowired
	public void setOwnerDao(OwnerDao ownerDao) {
		this.ownerDao = ownerDao;
	}
	/**
	 * @param publicProfileDao the publicProfileDao to set
	 */
	@Autowired
	public void setPublicProfileDao(PublicProfileDao publicProfileDao) {
		this.publicProfileDao = publicProfileDao;
	}

	/**
	 * @param oracleGUIDSource the oracleGUIDSource to set
	 */
	@Autowired(required=false)
	public void setOracleGUIDSource(OracleGUIDSource oracleGUIDSource) {
		this.oracleGUIDSource = oracleGUIDSource;
	}
	/**
	 * 
	 * @param ctcalxitemid
	 * @param model
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET)
	protected String showDetails(@RequestParam(value="id", required=false, defaultValue="") String ctcalxitemid, final ModelMap model) {
		model.addAttribute("id", ctcalxitemid);
		if(StringUtils.isNotBlank(ctcalxitemid)) {
			ICalendarAccount account = this.calendarAccountDao.getCalendarAccountFromUniqueId(ctcalxitemid);
			if(null != account) {
				model.addAttribute("isDelegate", account instanceof IDelegateCalendarAccount);
				model.addAttribute("calendarAccount", account);
				model.addAttribute("isAdvisor", IdentityUtils.isAdvisor(account));
				model.addAttribute("calendarAccountAttributes", account.getAttributes().entrySet());
				if(null != this.oracleGUIDSource) {
					String oracleGUID = this.oracleGUIDSource.getOracleGUID(account);
					model.addAttribute("oracleGUID", oracleGUID);
				}
				// try to look up visitor
				try {
					this.visitorDao.toVisitor(account);
					model.addAttribute("isVisitor", true);
				} catch (NotAVisitorException e) {
					// ignore
				}
				// try to look up scheduleowner
				IScheduleOwner owner = this.ownerDao.locateOwner(account);
				if(null != owner) {
					model.addAttribute("owner", owner);
					model.addAttribute("ownerPreferences", owner.getPreferences().entrySet());
					// if a scheduleowner, try to look up public profile
					PublicProfile profile = this.publicProfileDao.locatePublicProfileByOwner(owner);
					if(null != profile) {
						model.addAttribute("publicProfile", profile);
					}
				}
			}
		}
		return "admin/account-details";
	}
}
