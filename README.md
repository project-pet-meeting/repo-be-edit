#  📝Pet Meeting BE(사이드 프로젝트)
<img src="https://user-images.githubusercontent.com/110077343/224494171-bb8c7f80-7698-4ac4-ad45-a6df2277fd59.png"></img><br/>

📌 프로젝트 소개
-------------
반려동물 정보 공유 커뮤니티,
위치기반과 실시간 채팅으로 반려동물 산책 친구를 만들어주는 서비스를 제공합니다.


 
GitHub: https://github.com/project-pet-meeting/repo-be-edit      


:date: 제작기간
-------------   
2022.11.14 ~ 2023.02.12  

:family: 팀 구성원 소개 및 담당 기능 구축
-------------   
|이름|포지션|담당 기능 구현|
|------|---|---|
|김범석|BE|회원가입, 로그인(JWT, 카카오, 네이버, 이메일인증), 채팅(stomp, Redis), 댓글CRUD 기능 구현 및 서버, CI/CD(Jenkins) 관리|
|김하늘|BE|이미지 업로드(S3), 지도API, 게시글/모임/반려동물 정보 CRUD, 찜하기, 마이페이지, 카테고리 조회, 검색 기능 구현 및 ERD 관리|

:computer: 사이드 프로젝트 중점 목표   
-------------     
###  1) HATEOAS(Hypermedia As The Engine Of Application State)하이퍼미디어를 통해 상태 구동을 적용시켜 API 응답하기         
https://github.com/project-pet-meeting/repo-be-edit/blob/051741d16830e54d6ee98fbdf231b2b9c4414a53/src/main/java/sideproject/petmeeting/post/controller/PostController.java#L38-L54    
###  2) 테스트 코드 작성         
https://github.com/project-pet-meeting/repo-be-edit/blob/051741d16830e54d6ee98fbdf231b2b9c4414a53/src/test/java/sideproject/petmeeting/member/controller/MemberControllerTest.java#L1-L507   
###  3) Spring Rest Docs 적용         
<img src="https://user-images.githubusercontent.com/110077343/224490064-df0eaebe-cae5-4905-9b77-f9e5c8fcc02f.png"></img><br/>    
https://github.com/project-pet-meeting/repo-be-edit/blob/main/src/docs/asciidoc/index.adoc  
###  4) WebSocket, stomp, Redis를 이용한 채팅 기능 구현         
https://github.com/project-pet-meeting/repo-be-edit/blob/051741d16830e54d6ee98fbdf231b2b9c4414a53/build.gradle#L63-L76   
https://github.com/project-pet-meeting/repo-be-edit/blob/051741d16830e54d6ee98fbdf231b2b9c4414a53/src/main/java/sideproject/petmeeting/chat/controller/ChatController.java#L1-L108   
###  5) Jenkins 빌드 및 자동 배포 적용         
<img src="https://user-images.githubusercontent.com/110077343/224490847-baa46a09-6bfd-4b55-bdeb-fc651acc7df9.png"></img><br/>   


:computer: 프로젝트 주요 기능
-------------    

###  1) 회원가입 & 로그인: 이메일 인증을 통한 회원가입, 카카오/네이버 회원가입        
###  2) 위치 기반 모임: 모임 CRUD, 지도 API
###  3) 모임을 위한 채팅 기능 제공: 채팅
###  4) 반려동물 관련 정보 공유: 게시글 CRUD, 이미지 업로드, 찜하기, 댓글, 카테고리 조회, 검색      
###  5) 마이페이지, 반려동물 정보 확인   


:green_book: 와이어프레임
-------------
<img src="https://user-images.githubusercontent.com/110077343/224487461-bcba024c-da4e-40c3-b7eb-8a4945960962.jpg"></img><br/>
<img src="https://user-images.githubusercontent.com/110077343/224487477-fd5a66b0-679e-4e3b-8994-236c0096d466.png"></img><br/>
<img src="https://user-images.githubusercontent.com/110077343/224487552-8292718b-4fa9-4ffa-abc7-1eabd28c8f87.png"></img><br/>   


:blue_book: ERD
-------------
<img src="https://user-images.githubusercontent.com/110077343/224485295-b24ffd7b-bd62-484a-8b3b-007cd49b30bb.png"></img><br/>   

:orange_book: API
-------------
<img src="https://user-images.githubusercontent.com/110077343/224491766-e04b0a3f-a628-487a-8df0-1e7c27ae437c.png"></img><br/>   
<img src="https://user-images.githubusercontent.com/110077343/224491793-f68cb90c-b442-4587-bcc3-9b723af8e2bd.png"></img><br/>   



:construction_worker: 기술 스택 및 도구
-------------
협업 도구       
<img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=Notion&logoColor=white">
<img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=Git&logoColor=white">
<img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=GitHub&logoColor=white">
<img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=Postman&logoColor=white">


기술 스택   
<img src="https://img.shields.io/badge/Java11-007396?style=for-the-badge&logo=Java11&logoColor=white">
<img src="https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=Spring&logoColor=white">
<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=Spring Boot&logoColor=white">
<img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=Spring Security&logoColor=white">
<img src="https://img.shields.io/badge/JSON 웹 토큰-000000?style=for-the-badge&logo=JSON 웹 토큰&logoColor=white">  
<img src="https://img.shields.io/badge/Amazon EC2-FF9900?style=for-the-badge&logo=Amazon EC2&logoColor=white">
<img src="https://img.shields.io/badge/Amazon S3-569A31?style=for-the-badge&logo=Amazon S3&logoColor=white">
<img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white">
<img src="https://img.shields.io/badge/IntelliJ IDEA-000000?style=for-the-badge&logo=IntelliJ IDEA&logoColor=white">

