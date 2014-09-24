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
package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.magnum.dataup.model.*;
import org.magnum.dataup.model.VideoStatus.VideoState;



@Controller
public class VideoCrt{

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
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
	
	//private List<Video> videos = new CopyOnWriteArrayList<Video>();
	 private static final AtomicLong currentId = new AtomicLong(0L);
		
	private Map<Long,Video> videos = new HashMap<Long, Video>();
	
	public Video save(Video entity) {
		checkAndSetId(entity);
		videos.put(entity.getId(), entity);
		return entity;
	}

	private void checkAndSetId(Video entity) {
		if(entity.getId() == 0){
			entity.setId(currentId.incrementAndGet());
		}
	}
	
	private String getDataUrl(long videoId){
	        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
	        return url;
	        }

	private String getUrlBaseForLocalServer() {
			HttpServletRequest request = 
			    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			   String base = 
			      "http://"+request.getServerName() 
			      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
			   return base;
			}
   // @ExceptionHandler
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST, produces = "application/json")
	public @ResponseBody Video addVideo(@RequestBody Video v){
		Video saved = save(v);
		v.setDataUrl(getDataUrl(v.getId()));
		//response.addHeader("Content-Type", "application/");
		//response.setStatus(HttpServletResponse.SC_OK);
		return  saved;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH,method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList(){
		return videos.values();
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.GET)
	public @ResponseBody void getVideoData(@PathVariable("id") long id, HttpServletResponse response) throws IOException{

	    try {
			VideoFileManager vfm = VideoFileManager.get();
			if (!videos.containsKey(id))
				response.setStatus(404);
			else if (vfm.hasVideoData(videos.get(id))){
				Video v = videos.get(id);	
			//response.addHeader("Content-Type", v.getContentType ());
			vfm.copyVideoData(v, response.getOutputStream());}
			else {response.setStatus(404);}
	    }
			catch (IndexOutOfBoundsException e){
				
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				
			}
			
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.POST)
	
	public @ResponseBody VideoStatus setVideoData(@PathVariable("id") long id,
            @RequestParam(value="data") MultipartFile videoData, HttpServletResponse response) throws IOException{	
		VideoFileManager vfm = VideoFileManager.get();
		
		try {
			if (!videos.containsKey(id))
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			else {
			Video v = videos.get(id);
			//response.addHeader("Content-Type", v.getContentType ());
			vfm.saveVideoData(v, videoData.getInputStream());}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		VideoStatus state = new VideoStatus(VideoState.READY);
	return state;
	}
}
