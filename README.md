# 💁🏻‍♂️ What is ‘Climacast’?

<img width="1024" height="487" alt="climacast-logo" src="https://github.com/user-attachments/assets/d36fb8b0-cce4-469e-b342-54a7b6d7a996" />


- 개요

Open-Meteo의 Open API(https://open-meteo.com/en/docs)를 활용하여 대한민국 각 지역의 **실시간 날씨 정보를 제공**하는 서비스입니다. 현재 날씨 상태뿐만 아니라, 최대 3개월 전까지의 상세한 기상 기록을 지역별로 조회할 수 있어 장기적인 날씨 패턴 분석이 가능합니다.

사용자는 **이메일, 슬랙, SMS** 등 다양한 알림 채널 중 자신에게 가장 편리한 방식을 선택할 수 있습니다. 선택한 채널을 통해 관심 있는 지역의 일일 날씨 예보(Forecast)부터 과거 기상 데이터(History)까지 맞춤형으로 받아볼 수 있는 **구독형 서비스**를 제공합니다. 구독 설정은 언제든지 사용자의 필요에 따라 변경이 가능합니다.

또한, 사용자는 **AI 기반** 날씨 분석 API를 통해 특정 기간의 날씨 예보와 과거 날씨에 대한 종합적인 분석 및 요약 정보를 **REST** API 또는 **스트리밍** 형식(AI 챗봇)으로 받아볼 수 있습니다.


- 서비스 동작

<aside>

1. **배치 서버 스케줄러 실행**
    - 날씨 예보 데이터 : **30분** 주기로 실행
    - 날씨 히스토리 데이터 : **24시간** 주기로 실행
    - `Spring Batch`를 활용하여 주기적으로 데이터를 수집 및 저장
2. **전 지역에 대한 위/경도 리스트업**
    - 대상 지역 : 대한민국 **전역** (ex: 서울특별시 서대문구)
    - 사전 정의된 지역 목록을 기반으로 **CSV**에서 데이터를 로드
3. **각 위/경도를 기반으로 Open-Meteo Open API 호출**
    - 총 **252개** 지역에 대해 **비동기** 방식(`Reactor` 기반 `WebClient`)을 사용하여 Open-Meteo API 호출
    - Open API의 응답을 도시별 시간 단위 날씨 데이터로 변환
4. **호출 결과(날씨 데이터)를 저장**
    - MySQL : 과거 날씨 데이터를 저장하여 분석 및 통계 활용
    - Elasticsearch : 실시간 및 예보 데이터를 저장하여 빠른 검색 및 분석 제공
    - 대량의 데이터 처리를 위해 **JDBC Batch Update & Elasticsearch Bulk API** 활용
5. **구독 서버에서 유저별 스케줄러 실행 및 알림 전송**
    - 각 유저의 **구독 설정**(지역, 알림 방식, 알림 스케줄, 데이터 종류)에 따라 스케줄링 실행
    - 알림 전송 방식
        - Email : 등록된 이메일로 전송
        - Slack : `Webhook API` 를 활용하여 각 채널 구독자에게 전송
        - SMS : `Twilio API` 를 활용하여 등록된 번호로 전송
</aside>

- 구독 서비스
    - Email
      
        <img width="1692" height="1186" alt="email-example" src="https://github.com/user-attachments/assets/4c74911a-4989-4da3-837b-daffc8c0cfeb" />
        


    - Slack
      
        <img width="732" height="840" alt="slack-example" src="https://github.com/user-attachments/assets/25fb2283-29b2-4554-8421-fa2b2d2033a8" />



- Open-Meteo API
    
    https://github.com/open-meteo/open-meteo
    
    
    API 호출 URL(예시)
    
    ```json
    {
        "latitude": 37.55,
        "longitude": 127,
        "generationtime_ms": 0.015735626220703125,
        "utc_offset_seconds": 0,
        "timezone": "GMT",
        "timezone_abbreviation": "GMT",
        "elevation": 38.0,
        "hourly_units": {
            "time": "iso8601",
            "temperature_2m": "°C"
        },
        "hourly": {
            "time": [
                "2025-04-01T00:00",
                "2025-04-01T01:00",
                "2025-04-01T02:00",
                "2025-04-01T03:00",
                "2025-04-01T04:00",
                "2025-04-01T05:00",
    						...
            ],
            "temperature_2m": [
                4.6,
                4.1,
                3.4,
                2.0,
                1.6,
    						...
            ],
            ...
        }
    }
    ```
    
    ```json
    {
    	  "parentRegion": "서울특별시",
    	  "childRegion": "중구",
    	  "latitude": 37.55,
    	  "longitude": 127,
    	  "status": "CLEAR",
    	  "time": [2025, 4, 1, 0, 0],
    	  "weatherCode": 0,
    	  "temperature2m": 3.5,
    	  "temperature80m": 6.4,
    	  "temperature120m": 5.8,
    	  "temperature180m": 5.1,
    	  "windSpeed10m": 4.1,
    	  "windSpeed80m": 6.2,
    	  "windSpeed120m": 8.7,
    	  "windSpeed180m": 12,
    	  "humidity2m": 89
    }
    ```
    
- AI 날씨 분석 & 요약 API
    - [GET] /api/ai/weather/analyze
      
    <img width="2588" height="1124" alt="postman-1" src="https://github.com/user-attachments/assets/472dbb92-fbbf-4d1b-92e6-ca4f71abc111" />
    
    - [GET] /api/ai/weather/analyze/stream
      
    <img width="2590" height="1544" alt="postman-2" src="https://github.com/user-attachments/assets/c234f9f6-72fa-47ca-9b53-21fa43544397" />
    

- 서비스 모니터링
    - Prometheus + Grafana
      
        <img width="2818" height="1434" alt="grafana-1" src="https://github.com/user-attachments/assets/640e1148-f4e1-4862-b5f6-a5a785bb8d21" />
        <img width="2792" height="1268" alt="grafana-2" src="https://github.com/user-attachments/assets/eb2b28ab-701f-4d67-8c37-3e87b39980bc" />
        <img width="2878" height="1410" alt="grafana-3" src="https://github.com/user-attachments/assets/2bb1d596-8fcb-4141-a3cc-995e131cc880" />

        
    
    - Kafka UI
      
        <img width="2408" height="884" alt="kafkaui-1" src="https://github.com/user-attachments/assets/f38b261f-d4b2-40f3-9ae2-d0b39d4e33c0" />
        <img width="2342" height="1022" alt="kafkaui-2" src="https://github.com/user-attachments/assets/037efa34-e679-40f6-851f-4c38755f9fe1" />
        <img width="1864" height="498" alt="kafkaui-3" src="https://github.com/user-attachments/assets/fd8523f5-9ca0-4bd4-b3dd-92c7b89d8ba9" />



    - Lens (Kubernetes)
      
        <img width="1521" height="944" alt="lens-1" src="https://github.com/user-attachments/assets/d1698adb-3b5a-4d82-86ae-03adc91eed8b" />
        <img width="1516" height="289" alt="lens-2" src="https://github.com/user-attachments/assets/54a5719a-437d-4c0e-88cb-db1ea7311d4d" />

        

# 🛠️ Skills & Libraries

<img width="718" height="586" alt="skills" src="https://github.com/user-attachments/assets/b707641e-d7d6-4ec9-8466-aca55adcb561" />


# 📊 Project Architecture

- System Architecture
<img width="1910" height="1070" alt="aws" src="https://github.com/user-attachments/assets/4cab0688-dd9b-4877-8f76-c309e5bf553d" />



- Kubernetes Architecture

<img width="938" height="531" alt="k8s" src="https://github.com/user-attachments/assets/16f33ee4-da01-4755-bebd-261ab9aadc8b" />



- MSA Backend Architecture

<img width="1892" height="1050" alt="msa" src="https://github.com/user-attachments/assets/0132e3ff-36b7-486d-ae4f-9b19a4fa8f8c" />


# 📱API Document

[Climacast API 문서](https://www.notion.so/Climacast-API-224434f9d510809d994ac873b008f863?pvs=21)
