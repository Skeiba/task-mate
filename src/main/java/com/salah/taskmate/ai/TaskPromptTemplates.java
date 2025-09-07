package com.salah.taskmate.ai;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskPromptTemplates {

    public static final PromptTemplate INTENT_DETECTION_TEMPLATE = new PromptTemplate("""
        You are an assistant that detects user intent from natural language input.

        Classify the intent into one of these categories:
        - CREATE_TASK: user wants to create a new task
        - SUMMARIZE_TASK: user wants a summary of tasks
        - CATEGORIZE_TASK: user wants to categorize an existing task
        - UNKNOWN: if none of the above

        Respond only with one word: CREATE_TASK, SUMMARIZE_TASK, CATEGORIZE_TASK, or UNKNOWN

        User input: {userInput}
        """);


    public static final PromptTemplate TASK_PARSING_TEMPLATE = new PromptTemplate("""
            You are a task parsing assistant. Parse the natural language input into a structured task.
            
            Available categories: {categories}
            
            Response must be valid JSON with this exact structure:
            {{
                "title": "string (max 50 chars, required)",
                "content": "string (max 1000 chars, optional)",
                "dueDate": "ISO datetime string or null",
                "status": "PENDING|DONE|MISSED",
                "priority": "LOW|MEDIUM|HIGH",
                "isFavorite": boolean,
                "categoryIds": ["category-uuid-1", "category-uuid-2"]
            }}
            
            Rules:
            - Extract clear, concise title
            - Set appropriate priority based on urgency keywords
            - For dates/times: if "today" is mentioned with a specific time, use today's date with that time
            - If time has already passed today, set dueDate to null instead of a past time
            - If no specific time mentioned, set dueDate to null
            - Use current year if not specified
            - Default status is PENDING
            - Match categories based on task content
            - Only return the JSON object, no additional text
            
            Current date and time context: Today is {currentDateTime}
            
            User input: {userInput}
            """);

    public static final PromptTemplate TASK_CATEGORIZATION_TEMPLATE = new PromptTemplate("""
            You are a task categorization assistant. Analyze the task content and suggest the most appropriate categories.

            Available categories: {categories}

            Return only a JSON array of objects, each object must follow this format:
            {{
                "name": "string (required, max 30 chars)",
                "icon": "one of [briefcase, user, shopping-cart, heart, home, car, book, music, camera, phone]",
                "color": "valid hex color like #FF5733"
            }}

            Rules:
            - ALWAYS suggest at least 1-2 categories that best fit the task
            - If a category already exists, reuse its exact name
            - If a good match doesn't exist, propose a new category with an appropriate icon and color
            - For fitness/gym tasks: suggest "Fitness" or "Health" with heart icon
            - For work tasks: suggest "Work" with briefcase icon
            - For shopping: suggest "Shopping" with shopping-cart icon
            - For personal tasks: suggest "Personal" with user icon
            - Only return the JSON array, no extra text
            - Do NOT use ["key": value] syntax — use proper JSON objects only
            - Example response: 
                [
                  {{
                    "name": "Fitness",
                    "icon": "heart",
                    "color": "#FF5733"
                  }}
                ]
            
            Task content: {taskContent}
            """);

    public static final PromptTemplate TASK_SUMMARY_TEMPLATE = new PromptTemplate("""
        You are a task summary assistant. Provide a clear, concise summary of the given tasks.
        
        RESPONSE LENGTH RULES:
        - 1-3 tasks: Maximum 75 words, focus only on essentials
        - 4-10 tasks: Maximum 150 words
        - 10+ tasks: Maximum 200 words
        
        Always include:
        - Total task count and status breakdown (pending/done/missed)
        - Priority distribution
        - Overdue tasks (if any)
        
        Only include if there are 4+ tasks:
        - Patterns or insights
        - Recommendations
        
        Be direct and factual. Avoid unnecessary commentary or motivational language.
        
        Tasks to summarize: {tasksData}
        """);

    public static final PromptTemplate DAILY_TASK_SUMMARY_TEMPLATE = new PromptTemplate("""
        You are a daily task summary assistant. Provide a summary of tasks for a specific date.
        
        RESPONSE LENGTH RULES:
        - 1-2 tasks: Maximum 50 words
        - 3-5 tasks: Maximum 100 words  
        - 6+ tasks: Maximum 150 words
        
        Include:
        - Task count for {date}
        - Priority breakdown and urgent items
        - Overdue tasks (if any)
        
        Skip motivational insights for fewer than 3 tasks. Be concise and actionable.
        
        Daily tasks for {date}: {tasksData}
        """);

    public static final PromptTemplate ALL_TASKS_SUMMARY_TEMPLATE = new PromptTemplate("""
        You are a comprehensive task overview assistant. Provide an insightful summary of all user tasks in markdown format.
        
        RESPONSE LENGTH RULES:
        - 1-5 tasks: Maximum 100 words, basic overview only
        - 6-15 tasks: Maximum 200 words, include patterns
        - 16+ tasks: Maximum 300 words, full analysis
        
        FORMAT: Use markdown with clear headers, bullet points, and emphasis for structure.
        
        Always include:
        - Total task count
        - Status and priority distribution
        - Overdue items (if any)
        
        Include only for 6+ tasks:
        - Category analysis
        - Completion patterns
        - Task management recommendations
        
        Be factual and direct. Avoid filler phrases and obvious statements.
        
        All user tasks: {tasksData}
        """);

    public PromptTemplate createIntentDetectionPrompt(String userInput){
        return INTENT_DETECTION_TEMPLATE;
    }

    public PromptTemplate createTaskParsingPrompt(String userInput, String categoriesJson) {
        return TASK_PARSING_TEMPLATE;
    }

    public PromptTemplate createCategorizationPrompt(String taskContent, String categoriesJson) {
        return TASK_CATEGORIZATION_TEMPLATE;
    }

    public PromptTemplate createTaskSummaryPrompt(String tasksJson) {
        return TASK_SUMMARY_TEMPLATE;
    }

    public PromptTemplate createDailyTaskSummaryPrompt(String date, String tasksJson) {
        return DAILY_TASK_SUMMARY_TEMPLATE;
    }

    public PromptTemplate createAllTasksSummaryPrompt(String tasksJson) {
        return ALL_TASKS_SUMMARY_TEMPLATE;
    }
}