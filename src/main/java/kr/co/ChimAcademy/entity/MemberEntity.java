package kr.co.ChimAcademy.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "member")
public class MemberEntity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@Column(name = "uid")
	private String uid;
	@JsonIgnore
	private String pass;
	
	private String name;
	private String birth;
	private String depCode;
	private String majorCode;
	private int sYear;
	private int gender;
	private int totCredit;
	private int avgCredit;
	private String hp;
	private String email;
	private int status;
	private int level;
	private String zip;
	private String addr1;
	private String addr2;
	private String regip;
	private String wdate;
	@LastModifiedDate
	private LocalDateTime rdate;
	private String info;
	private String profile;
	private String member2;
	private String member3;

}
