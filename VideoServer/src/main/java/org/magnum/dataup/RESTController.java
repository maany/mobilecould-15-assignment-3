package org.magnum.dataup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * This class is a Spring Controller that handles requests to the following endpoints :
 * <ul>
 * <li>GET /video
 * <li>POST /video
 * <li>GET /video/{id}/data
 * <li>POST /video/{id}/data
 * </ul>
 * 
 * @author Mayank
 */
@Controller
public class RESTController {
	/**
	 * {@link java.util.List} stores the {@link Video} objects.
	 */
	List<Video> videos = new ArrayList<Video>();
	
//==========================
//	Begin Handler Mappings
//==========================
	@RequestMapping(value="/video",method=RequestMethod.GET)
	public @ResponseBody List<Video> fetchVideoMetaData(){
		return videos;
	}
	
	@RequestMapping(value="/video",method=RequestMethod.POST)
	public @ResponseBody Video addVideoMetaData(@RequestBody Video video,HttpServletRequest request){
		// Generate unique ID
		video.setId(generateRandomId(video));
		// Generate dataUrl
		video.setDataUrl(generateDataUrl(video,request));
		videos.add(video);
		return video;
	}

	@RequestMapping(value="/video/{id}/data", method=RequestMethod.POST)
	public @ResponseBody VideoStatus uploadVideo(@PathVariable("id") String id,@RequestParam("data") MultipartFile file, HttpServletResponse response){
		try {
			VideoFileManager manager = VideoFileManager.get();
			Video video = getVideoById(id);
			if(video==null)
				throw new Exception("Video Metadata not found"); // video has not been uploaded yet
			manager.saveVideoData(video, file.getInputStream());
			
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			e.printStackTrace();
			return null;
		} catch(Exception ex){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		return new VideoStatus(VideoState.READY);
	}
	
	@RequestMapping(value="/video/{id}/data", method=RequestMethod.GET)
	public void sendVideoToClient(@PathVariable String id,HttpServletResponse response){
		response.setContentType("multipart/mp4");
		ServletOutputStream out=null;
		Video video=null;
		video = getVideoById(id);
		try {
			if(video==null)
				throw new RuntimeException();
			out = response.getOutputStream();
			VideoFileManager.get().copyVideoData(video, out);
			
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			e.printStackTrace();
		}catch(Exception ex){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
//==========================
//	End Handler Mappings
//==========================
	
	
	/**
	 * Randomly generate a {@link Long} value for id.
	 * @param video
	 * @return
	 */
	
	private long generateRandomId(Video video){
		Date date = new Date();
		return date.getTime();
	}
	/**
	 * Return the dataURL for the {@link Video} object
	 * @param video
	 * @param request
	 * @return
	 */
	private String generateDataUrl(Video video, HttpServletRequest request) {
		String base = 
	              "http://"+request.getServerName() 
	              + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		String url = base + "/video/" + video.getId() + "/data"; 
		return url;
	}
	/**
	 * returns the {@link Video} object associated with the id.
	 * @param id
	 * @return
	 */
	
	private Video getVideoById(String id) {
		Video video = null;
		Long videoId = Long.parseLong(id);
		for (Video temp:videos){
			if(temp.getId()==videoId){
				video=temp;
				break;
			}
		}
		return video;
	}
}
