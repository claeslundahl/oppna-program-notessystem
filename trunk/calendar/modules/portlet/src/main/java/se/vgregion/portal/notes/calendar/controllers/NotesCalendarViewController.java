/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.portal.notes.calendar.controllers;

import java.util.List;
import java.util.Locale;

import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.context.PortletConfigAware;

import se.vgregion.core.domain.calendar.CalendarEvents;
import se.vgregion.core.domain.calendar.CalendarEventsPeriod;
import se.vgregion.core.domain.calendar.CalendarItem;
import se.vgregion.services.calendar.CalendarService;

@Controller
@SessionAttributes("displayPeriod")
@RequestMapping("VIEW")
public class NotesCalendarViewController implements PortletConfigAware {
    private static final String TIME_FORMAT = "dd MMMM";
    public static final String VIEW_WEEK = "week";
    private CalendarService calendarService;
    private PortletConfig portletConfig = null;
    private PortletData portletData;

    /**
     * Constructs a NotesCalendarViewController
     * 
     * @param calendarService
     */
    @Autowired
    public NotesCalendarViewController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public void setPortletConfig(PortletConfig portletConfig) {
        this.portletConfig = portletConfig;
    }

    @Autowired
    public void setPortletData(PortletData portletData) {
        this.portletData = portletData;
    }

    /**
     * Displays the calendar events for the logged in user.
     * 
     * @param model
     *            the model
     * @param request
     *            the portletRequest
     * @param response
     *            the portletResponse
     * @return the view to display
     */
    @RenderMapping
    public String displayCalendarEvents(ModelMap model, RenderRequest request, RenderResponse response) {
        String userId = portletData.getUserId(request);
        String title = portletData.getPortletTitle(portletConfig, request);
        CalendarEvents events = null;
        CalendarEventsPeriod displayPeriod = (CalendarEventsPeriod) model.get("displayPeriod");
        if (displayPeriod == null) {
            displayPeriod = new CalendarEventsPeriod(new DateTime(), CalendarEventsPeriod.DEFAULT_PERIOD_LENGTH);
            model.put("displayPeriod", displayPeriod);
        }
        events = calendarService.getCalendarEvents(userId, displayPeriod);
        List<List<CalendarItem>> calendarItems = events.getCalendarItemsGroupedByStartDate();
        portletData.setPortletTitle(response, title + " "
                + getFormatedDateIntervallToTitle(displayPeriod, response.getLocale()));
        model.put("calendarItems", calendarItems);
        return VIEW_WEEK;
    }

    private String getFormatedDateIntervallToTitle(CalendarEventsPeriod displayPeriod, Locale locale) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(TIME_FORMAT).withLocale(locale);
        StringBuilder title = new StringBuilder(TIME_FORMAT.length() * 2 + " - ".length());

        title.append(formatter.print(displayPeriod.getStartDate()));
        title.append(" - ");
        title.append(formatter.print(displayPeriod.getEndDate()));

        return title.toString();
    }

    /**
     * Action method to step one period ahead.
     * 
     * @param model
     *            the model
     */
    @ActionMapping(params = "navigate=next")
    public void nextWeek(ModelMap model) {
        CalendarEventsPeriod displayPeriod = (CalendarEventsPeriod) model.get("displayPeriod");
        if (displayPeriod != null) {
            model.put("displayPeriod", displayPeriod.next());
        }
    }

    /**
     * Action method to step one period back.
     * 
     * @param model
     *            the model
     */
    @ActionMapping(params = "navigate=previous")
    public void previousWeek(ModelMap model) {
        CalendarEventsPeriod displayPeriod = (CalendarEventsPeriod) model.get("displayPeriod");
        if (displayPeriod != null) {
            model.put("displayPeriod", displayPeriod.previous());
        }
    }

}
