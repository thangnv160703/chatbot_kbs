package practice.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import practice.model.Answer;
import practice.model.NgheNghiep;
import practice.model.Result;
import practice.repository.NgheNghiepRepository;

@Service
@Slf4j
public class CareerService {
	@Autowired
	private NgheNghiepRepository ngheNghiepRepo;

	public ArrayList<Answer> findBestCareer(String body) {

//		ArrayList<NgheNghiep> career_list = (ArrayList<NgheNghiep>) ngheNghiepRepo.findByCode("ACE");
//		log.info(career_list.toString());

		// Tach chuoi json
		String[] str = body.substring(1, body.length() - 2).split("},");
		JsonParser springParser = JsonParserFactory.getJsonParser();
		Map<String, Object> map;
		Map<String, Integer> dsKetQua = new HashMap<>();

		for (int i = 0; i < str.length; ++i) {
			map = springParser.parseMap(str[i] + "}");
			dsKetQua.put(map.get("name").toString(), Integer.parseInt(map.get("kq").toString()));

		}

		// lay nhung chi so khac 0
		ArrayList<Result> list = new ArrayList<>();
		for (String index : dsKetQua.keySet()) {
			if (dsKetQua.get(index) != 0) {
				list.add(new Result(index, dsKetQua.get(index)));
			}
		}
		Collections.sort(list); // sap xep

		ArrayList<String> code_list = getHollandCode(list);
		log.info("Ket qua: " + code_list.toString());

		ArrayList<Answer> answer_list = new ArrayList<>();
		for (String code : code_list) {
			answer_list.add(getAnswer(code));
		}

//		log.info("Answer_list:");
//		for (Answer answer : answer_list) {
//			log.info(answer.toString());
//		}

		return answer_list;
	}

	public Answer getAnswer(String hollandCode) {
		Answer answer = new Answer(hollandCode);
		ArrayList<NgheNghiep> job_list = (ArrayList<NgheNghiep>) ngheNghiepRepo.findByCode(hollandCode);
		for (NgheNghiep job : job_list) {
			answer.addNghe(job);
		}
		return answer;
	}

	public ArrayList<String> getHollandCode(ArrayList<Result> list) {
		for(Result re:list) {
			log.info(re.toString());
		}
		
		ArrayList<String> result_list = new ArrayList<>();
		if (list.isEmpty()) {

		} else if (list.size() == 1) {
			result_list.add(list.get(0).getId());
		} else if (list.size() == 2) {
			if (list.get(0).getScore() == list.get(1).getScore()) {
				result_list.add(list.get(0).getId() + list.get(1).getId());
				result_list.add(list.get(1).getId() + list.get(0).getId());
			} else {
				result_list.add(list.get(0).getId() + list.get(1).getId());
			}
		} else {
			int loc = 3;
			while (loc < list.size() && (list.get(2).getScore() == list.get(loc).getScore())) {
				loc += 1;
			}
			if (list.get(0).getScore() == list.get(1).getScore()) {
				if (list.get(1).getScore() == list.get(2).getScore()) {
					String str = "";
					for (int i = 0; i < loc; ++i) {
						str += list.get(i).getId();
					}
					ArrayList<String> list2 = permute(str);
					HashSet<String> set = new HashSet<>();
					for (String sub : list2) {
						set.add(sub.substring(0, 3));
					}
					for (String x : set) {
						result_list.add(x);
					}
				} else {
					String str1 = list.get(0).getId() + list.get(1).getId();
					String str2 = list.get(1).getId() + list.get(0).getId();
					for (int i = 2; i < loc; ++i) {
						result_list.add(str1 + list.get(i).getId());
						result_list.add(str2 + list.get(i).getId());
					}
				}
			} else {
				if (list.get(1).getScore() == list.get(2).getScore()) {
					String str = "";
					for (int i = 1; i < loc; ++i) {
						str += list.get(i).getId();
					}
					ArrayList<String> list2 = permute(str);
					HashSet<String> set = new HashSet<>();
					for (String sub : list2) {
						set.add(sub.substring(0, 2));
					}
					for (String x : set) {
						result_list.add(list.get(0).getId() + x);
					}
				} else {
					String str1 = list.get(0).getId() + list.get(1).getId();
					for (int i = 2; i < loc; ++i) {
						result_list.add(str1 + list.get(i).getId());
					}
				}
			}
		}

		if (list.size() < 3) {
			return result_list;
		} else {
			return getDetail(result_list);
		}
	}

	public ArrayList<String> getDetail(ArrayList<String> result_list) {
//		log.info(result_list.toString());
		HashMap<String, Integer> map = new HashMap<>();
		map.put("R", 0);
		map.put("I", 1);
		map.put("A", 2);
		map.put("S", 3);
		map.put("E", 4);
		map.put("C", 5);
		int[][] matrix = { { 0, 1, 0, -1, 0, 1 }, { 1, 0, 1, 0, -1, 0 }, { 0, 1, 0, 1, 0, -1 }, { -1, 0, 1, 0, 1, 0 },
				{ 0, -1, 0, 1, 0, 1 }, { 1, 0, -1, 0, 1, 0 }, };

		int max_value = -10;
		HashMap<String, Integer> priority = new HashMap<>();
		for (String result : result_list) {
			int first = (int) map.get(result.substring(0, 1));
			int second = (int) map.get(result.substring(1, 2));
			int third = (int) map.get(result.substring(2));
			int value = 3 * matrix[first][second] + 2 * matrix[first][third] + matrix[second][third];
			log.info(result + ":" + value);
			if (max_value < value) {
				max_value = value;
				priority.clear();
				priority.put(result, value);
			} else if (max_value == value) {
				priority.put(result, value);
			}
		}

//		for (String s : priority.keySet()) {
//			log.info(s);
//		}

		ArrayList<String> final_result = new ArrayList<>();

		for (String s : priority.keySet()) {
			final_result.add(s);
		}

//		log.info("Final result: ", final_result.toString());

		return final_result;
	}

	public ArrayList<String> permute(String input) {
		int inputLength = input.length();
		boolean[] used = new boolean[inputLength];
		StringBuffer outputString = new StringBuffer();
		char[] in = input.toCharArray();
		ArrayList<String> list = new ArrayList<>();
		doPermute(in, outputString, used, inputLength, 0, list);
		return list;
	}

	public void doPermute(char[] in, StringBuffer outputString, boolean[] used, int inputLength, int level,
			ArrayList<String> list) {
		if (level == inputLength) {
			list.add(outputString.toString());
			return;
		}
		for (int i = 0; i < inputLength; ++i) {
			if (used[i]) {
				continue;
			}
			outputString.append(in[i]);
			used[i] = true;
			doPermute(in, outputString, used, inputLength, level + 1, list);
			used[i] = false;
			outputString.setLength(outputString.length() - 1);
		}
	}
}
