/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.auth.User;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class Assign2Controller {
	
	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
		
	@Autowired
	private VideoRepository videos;

	// Receives POST requests to /video and converts the HTTP
	// request body, which should contain json, into a Video
	// object before adding it to the list. The @RequestBody
	// annotation on the Video parameter is what tells Spring
	// to interpret the HTTP request body as JSON and convert
	// it into a Video object to pass into the method. The
	// @ResponseBody annotation tells Spring to conver the
	// return value from the method back into JSON and put
	// it into the body of the HTTP response to the client.
	//
	// The VIDEO_SVC_PATH is set to "/video" in the VideoSvcApi
	// interface. We use this constant to ensure that the 
	// client and service paths for the VideoSvc are always
	// in synch.
	//1 POST /video (addVideo controller method)
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v){
		 
		 return videos.save(v);
	}
	
	// Receives GET requests to /video and returns the current
	// list of videos in memory. Spring automatically converts
	// the list of videos to JSON because of the @ResponseBody
	// annotation.
	//2 GET /video (getVideoList controller method)
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList(){
		return Lists.newArrayList(videos.findAll());
	}
	
	//3   GET /video/{id} (getVideoById controller method)
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}", method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id, HttpServletResponse response){
		Video v= null;
		if(videos.findOne(id)==null)
		{
			response.setStatus(404);
		} else {
		v = videos.findOne(id);
		}
		return v;
	}
	
	//4  GET /video/search/findByName?title={title} (findByTitle controller method)
	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(
			// Tell Spring to use the "title" parameter in the HTTP request's query
			// string as the value for the title method parameter
			@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title, HttpServletResponse response
	){
		Collection<Video> v = null;
		if (videos.findByName(title) == null){
			response.setStatus(404);
		} else {
		v = videos.findByName(title);
	
		}
		return v;
	}
	
	//5  GET /video/search/findByDurationLessThan?duration={duration} (findByDurationLessThan controller method)
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration, HttpServletResponse response) {
		
		Collection<Video> v = null;
		if (videos.findByDurationLessThan(duration)==null){
			response.setStatus(404);
		} else {
			v = videos.findByDurationLessThan(duration);
		}
		
		return v;
	}

	//6.      POST /video/{id}/like (likeVideo controller method)
	@PreAuthorize("hasRole(USER)")
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/like", method=RequestMethod.POST)
	public @ResponseBody void setLikeVideo(@PathVariable("Id") long id, Principal p, HttpServletResponse response) throws IOException{
	Video v =null; 
	if (!videos.exists(id)){
		 response.setStatus(404);
	 }
	else {
	 v = videos.findOne(id);
	 Set<String> likesUserNames = v.getLikesUsernames();
	 if (likesUserNames.contains(p.getName())){
		 System.out.println("This is like again"+ p.getName());
		// response.setStatus(400);
		 new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
	 } else {
	

			// keep track of users have liked a video
			  likesUserNames.add(p.getName());
			  v.setLikesUsernames(likesUserNames);
			  v.setLikes(likesUserNames.size());
			  videos.save(v);
			  response.setStatus(200);
	 		}
		}
	}  
	  
	  
	//7.      POST /video/{id}/unlike (unlikeVideo controller method)
	@PreAuthorize("hasRole(USER)")
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/unlike", method=RequestMethod.POST)
	public @ResponseBody void setUnLikeVideo(@PathVariable("Id") long id, Principal p, HttpServletResponse response ){
		 if (!videos.exists(id)){
			 response.setStatus(404);
		 }
		 Video v = videos.findOne(id);
		 Set<String> likesUserNames = v.getLikesUsernames();
		 if (likesUserNames.contains(p)){
			 likesUserNames.remove(p.getName());
			 v.setLikes(likesUserNames.size());
			 videos.save(v);
			 response.setStatus(200);
		 } else {
			 response.setStatus(400);
		 }
		 
	}
	
	//8.      GET /video/{id}/likedby (getUsersWhoLikedVideo controller method)
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/likeby", method=RequestMethod.GET)
	public @ResponseBody Iterator<String> getLikeBy(@PathVariable("Id") long id,HttpServletResponse response){
		 if (!videos.exists(id)){
			 response.setStatus(404);
		 }
		 Video v = videos.findOne(id);
		 Set<String> likesUserNames = v.getLikesUsernames();
		 Iterator<String> u = likesUserNames.iterator(); 
		return u;
	}
}
