package kr.co.ChimAcademy.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.co.ChimAcademy.dao.EbookDAO;
import kr.co.ChimAcademy.vo.CountVO;
import kr.co.ChimAcademy.vo.EbookCate1VO;
import kr.co.ChimAcademy.vo.EbookCate2VO;
import kr.co.ChimAcademy.vo.EbookFileVO;
import kr.co.ChimAcademy.vo.EbookVO;
import kr.co.ChimAcademy.vo.Ebook_ArticleVO;
import kr.co.ChimAcademy.vo.Ebook_Article_fileVO;
import kr.co.ChimAcademy.vo.MylibVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EbookService {
	
	@Autowired
	private EbookDAO dao;
	
	public List<EbookCate1VO> selectCate1s(){
		return dao.selectCate1s();
	};
	public List<EbookCate2VO> selectCate2s(int c1){
		return dao.selectCate2s(c1);
	};
	
	@Transactional
	public int insertEbook(EbookVO vo) {
		// 도서번호 등록
		String count = dao.selectCountTotal("2",vo) + "";
		String bookId = vo.getBookId()+count;
		vo.setBookId(bookId);
		// 책파일 업로드
		List<EbookFileVO> fvos = fileUpload(vo);
		// 책등록
		EbookFileVO thumb = fvos.get(1);
		String thumbLocation = "/elibFile/thumbFile/" + thumb.getNewName();
		vo.setThumb(thumbLocation);
		int result = dao.insertEbook(vo);
		// 책파일 등록
		EbookFileVO epub = fvos.get(0);
		if(epub != null) {
			dao.insertEbookFile(epub);
		}
		return result;
	};
	public CountVO selectCountEbooks() {
		return dao.selectCountEbooks();
	};
	public List<EbookVO> selectEbooks(String sort,String type,EbookVO vo,int start){
		return dao.selectEbooks(sort,type,vo,start);
	};
	public EbookVO selectEbook(String bookId) {
		return dao.selectEbook(bookId);
	};
	public int updateEbookLoan(int sort,String bookId) {
		return dao.updateEbookLoan(sort, bookId);
	};
	public int updateEbookReserv(int sort,String bookId) {
		return dao.updateEbookReserv(sort, bookId);
	};
	// 내서재 //////////////////////////////////////////
	public int insertMylib(MylibVO vo) {
		return dao.insertMylib(vo);
	};
	public List<MylibVO> selectMylibs(String uid, String state, int start){
		return dao.selectMylibs(uid,state,start);
	};
	public EbookFileVO selectEbookFile(String bookId) {
		return dao.selectEbookFile(bookId);
	};
	public int updateEbookDown(String bookId) {
		return dao.updateEbookDown(bookId);
	};
	public int updateEbookLike(String bookId) {
		return dao.updateEbookLike(bookId);
	};
	// 책파일 업로드 /////////////////////////////////////////

	private String EbookUploadPath = "elibFile/ebookFile/"; // 프로젝트 내 가상 경로
	private String ThumbUploadPath = "elibFile/thumbFile/"; // 프로젝트 내 가상 경로
	
	public List<EbookFileVO> fileUpload(EbookVO vo) {
		// 책 파일 첨부
		MultipartFile epub = vo.getFname();
		// 썸네일 파일 첨부 
		MultipartFile thumb = vo.getTname();
		List<MultipartFile> files = new ArrayList<>();
		files.add(epub);
		files.add(thumb);
		List<EbookFileVO> fvos = new ArrayList<>();
		int idx = 0;
		for(MultipartFile file : files) {
			if(!file.isEmpty()) {
				// 새 파일명 생성
				String oName = file.getOriginalFilename();
				String ext = oName.substring(oName.lastIndexOf("."));
				String nName = UUID.randomUUID().toString()+ext;
				String path = null;
				// 시스템 경로
				if(idx == 0) {
					path = new File(EbookUploadPath).getAbsolutePath();
				}else {
					path = new File(ThumbUploadPath).getAbsolutePath();
				}
				
				// 파일 저장
				try {
					file.transferTo(new File(path, nName));
				} catch (IllegalStateException e) {
					log.error(e.getMessage());
				} catch (IOException e) {
					log.error(e.getMessage());
				}
				
				EbookFileVO fvo = EbookFileVO.builder()
												.parent(vo.getBookId())
												.oriName(oName)
												.newName(nName)
												.build();
				fvos.add(fvo);
				idx++;
			}
		}
		return fvos;
	}
	// PDF 열기 //////////////////////////////////////////////////////
	public ResponseEntity<Resource> fileOpen(EbookFileVO vo) throws IOException {
		// String path = new File(uploadPath).getAbsolutePath()+"/"+vo.getNewName();
		Path path = Paths.get("elibFile/ebookFile/"+vo.getNewName());
		// String contentType = Files.probeContentType(path);
		HttpHeaders headers = new HttpHeaders(); // 스프링에서 제공 : HttpHeaders, ResponseEntity
		headers.setContentDisposition(ContentDisposition
														.builder("inline")
														.filename(vo.getOriName(), StandardCharsets.UTF_8)
														.build());
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF.toString());
		
		Resource resource = new InputStreamResource(Files.newInputStream(path));
		
		return new ResponseEntity<>(resource, headers, HttpStatus.OK); // res 객체
	}
	// 페이징 처리 시작 ///////////////////////////////////////////////////////
	// 현재 페이지 번호
	public int getCurrnetPage(String pg) {
		int currentPage = 1;
		if(pg != null) {
			currentPage = Integer.parseInt(pg);
		}
		return currentPage;
	}
	// 페이지 시작값
	public int getLimitStart(int currentPage) {
		return (currentPage-1) * 10;
	}
	// 전차잭/오디오 총 갯수
	public int selectCountTotal(String sort, EbookVO vo) {
		return dao.selectCountTotal(sort,vo);
	}
	// 내서재 총 갯수
	public int selectCountTotalMylibs(String uid, String state) {
		return dao.selectCountTotalMylibs(uid, state);
	};
	// 마지막 페이지 번호
	public int getLastPageNum(int total) {
		int lastPageNum = 0;
		if(total % 10 == 0){
			lastPageNum = total / 10;
		}else{
			lastPageNum = total / 10 + 1;
		}
		return lastPageNum;
	}
	// 시작 페이지 번호
	public int getPageStartNum(int total, int start) {
		return total - start;
	}
	// 페이지 그룹
	public int[] getPageGroup(int currentPage, int lastPageNum) {
		int currentPageGroup = (int)Math.ceil(currentPage / 10.0);
		int pageGroupStart = (currentPageGroup - 1) * 10 + 1;
		int pageGroupEnd = currentPageGroup * 10;
		if(pageGroupEnd > lastPageNum){
			pageGroupEnd = lastPageNum;
		}
		int[] result = {pageGroupStart,pageGroupEnd};
		return result;
	}
	// 페이징 처리 끝 ///////////////////////////////////////////////////////
}
